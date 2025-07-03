package ponder.steps.io

import kabinet.utils.randomUuidStringId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.appUserId
import ponder.steps.db.AnswerDao
import ponder.steps.db.AnswerEntity
import ponder.steps.db.IntentDao
import ponder.steps.db.StepLogDao
import ponder.steps.db.StepLogEntity
import ponder.steps.db.PathStepDao
import ponder.steps.db.QuestionDao
import ponder.steps.db.StepDao
import ponder.steps.db.TrekDao
import ponder.steps.db.TrekEntity
import ponder.steps.db.toEntity
import ponder.steps.model.data.Intent
import ponder.steps.model.data.IntentId
import ponder.steps.model.data.NewAnswer
import ponder.steps.model.data.PathStep
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepLogId
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.Trek
import ponder.steps.model.data.TrekId
import kotlin.time.Duration.Companion.minutes

class LocalTrekRepository(
    private val trekDao: TrekDao = appDb.getTrekDao(),
    private val stepDao: StepDao = appDb.getStepDao(),
    private val pathStepDao: PathStepDao = appDb.getPathStepDao(),
    private val intentDao: IntentDao = appDb.getIntentDao(),
    private val stepLogDao: StepLogDao = appDb.getLogDao(),
    private val questionDao: QuestionDao = appDb.getQuestionDao(),
    private val answerDao: AnswerDao = appDb.getAnswerDao(),
) : TrekRepository {

    suspend fun setOutcome(
        trekId: TrekId,
        step: Step,
        outcome: StepOutcome?,
        breadcrumbs: List<Step>?
    ): StepLogId? {
        val now = Clock.System.now()
        val logId = if (outcome != null) {
            val logId = randomUuidStringId()
            stepLogDao.insert(
                StepLogEntity(
                    id = logId,
                    stepId = step.id,
                    trekId = trekId,
                    pathStepId = step.pathStepId,
                    outcome = outcome,
                    updatedAt = now,
                    createdAt = now
                )
            )
            logId
        } else {
            val pathStepId = step.pathStepId
            if (pathStepId != null) {
                stepLogDao.deletePathStepLog(trekId, step.id, pathStepId)
            } else {
                stepLogDao.deletePathLog(trekId, step.id)
            }
            null
        }

        setPathOutcomes(
            trekId = trekId,
            step = step,
            breadcrumbs = breadcrumbs,
            outcome = outcome
        )

        return logId
    }

    suspend fun setPathOutcomes(
        trekId: TrekId,
        step: Step,
        breadcrumbs: List<Step>?,
        outcome: StepOutcome?
    ) {
        val trek = trekDao.readTrekById(trekId) ?: error("trekId not found")

        val isTopLevel = breadcrumbs == null
        val breadcrumbs = breadcrumbs ?: listOf(step)

        var status: StepStatus = StepStatus.Unfinished
        if (outcome == null) {
            // mark as incomplete down the chain of breadcrumbs
            stepLogDao.deletePathLogs(trekId, breadcrumbs.map { it.id })
        } else {
            // mark as complete up the chain of breadcrumbs
            for (step in breadcrumbs.reversed()) {
                val pathId = step.id
                val pathSteps = pathStepDao.readPathStepsByPathId(pathId)
                val logs = if (isTopLevel) stepLogDao.readTopLevelLog(trekId, step.id)
                    else stepLogDao.readTrekLogsByPathId(trekId, pathId)
                status = getStatus(trek, pathSteps, logs)
                if (status != StepStatus.Completed) break
                if (pathSteps.isNotEmpty()) {
                    val logId = randomUuidStringId()
                    val now = Clock.System.now()
                    stepLogDao.insert(
                        StepLogEntity(
                            id = logId,
                            stepId = pathId,
                            trekId = trekId,
                            pathStepId = step.pathStepId,
                            outcome = outcome,
                            updatedAt = now,
                            createdAt = now
                        )
                    )
                }
            }
        }

        trekDao.update(
            when (status) {
                StepStatus.Unfinished -> trek.copy(
                    isComplete = false,
                    finishedAt = null
                )

                StepStatus.Finished -> trek.copy(
                    isComplete = false,
                    finishedAt = trek.finishedAt ?: Clock.System.now()
                )

                StepStatus.Completed -> trek.copy(
                    isComplete = true,
                    finishedAt = trek.finishedAt ?: Clock.System.now()
                )
            }.toEntity()
        )
    }

    private suspend fun getStatus(
        trek: Trek,
        pathSteps: List<PathStep>,
        logs: List<StepLog>,
    ): StepStatus {
        if (pathSteps.isEmpty()) {
            // no log found for single-step trek
            val trekStepLog = logs.firstOrNull { it.stepId == trek.rootId } ?: return StepStatus.Unfinished
            // single log found is skipped (not expecting answers)
            if (trekStepLog.outcome == StepOutcome.Skipped) return StepStatus.Completed
            val questions = questionDao.readQuestionsByStepId(trek.rootId)
            // no step questions (not expecting answers)
            if (questions.isEmpty()) return StepStatus.Completed
            val answers = answerDao.readAnswersByLogIds(logs.map { it.id })
            // all questions answered or skipped
            if (answers.any { it.stepLogId == trekStepLog.id }) return StepStatus.Completed
            // still expecting answers
            else return StepStatus.Finished
        }
        // there's a pathstep without a log
        if (pathSteps.any { ps -> logs.all { l -> l.pathStepId != ps.id } }) return StepStatus.Unfinished
        val unskippedStepIds =
            pathSteps.mapNotNull { ps -> if (logs.all { l -> l.outcome != StepOutcome.Skipped }) ps.stepId else null }
        val questions = questionDao.readQuestionsByStepIds(unskippedStepIds)
        val answers = answerDao.readAnswersByLogIds(logs.map { it.id })
        // all questions answered or skipped
        if (questions.all { q -> answers.any { a -> a.questionId == q.id } }) return StepStatus.Completed
        // still expecting answers
        else return StepStatus.Finished
    }

    suspend fun createAnswer(trekId: TrekId, step: Step, answer: NewAnswer, breadcrumbs: List<Step>?): Boolean {
        val id = randomUuidStringId()
        val answer = AnswerEntity(
            id = id,
            stepLogId = answer.stepLogId,
            questionId = answer.questionId,
            value = answer.value,
            type = answer.type,
            updatedAt = Clock.System.now()
        )
        val isSuccess = answerDao.insert(answer) != -1L
        if (isSuccess) {
            setPathOutcomes(trekId, step, breadcrumbs, StepOutcome.Completed)
        }
        return isSuccess
    }

    suspend fun readTrekFinishedAt(intentIds: List<IntentId>) = trekDao.readTrekFinishedAt(intentIds)

    suspend fun readTreksLastStartedAt(intentIds: List<IntentId>) = trekDao.readTreksLastStartedAt(intentIds)

    suspend fun createTrekFromIntent(intent: Intent): TrekId {
        val id = randomUuidStringId()
        val now = Clock.System.now()

        trekDao.create(
            TrekEntity(
                id = id,
                userId = appUserId,
                intentId = intent.id,
                rootId = intent.rootId,
                isComplete = false,
                startedAt = now,
                finishedAt = null,
                expectedAt = intent.expectedMins?.let { mins -> Clock.System.now() + mins.minutes },
                updatedAt = now
            ))

        return id
    }

    suspend fun readTrekById(trekId: TrekId) = trekDao.readTrekById(trekId)

    fun flowRootTodoSteps(start: Instant) = trekDao.flowRootTodoSteps(start)

    fun flowActiveTreks(start: Instant) = trekDao.flowActiveTreks(start)

    fun flowRootProgress(start: Instant) = trekDao.flowRootProgresses(start)

    fun flowPathProgresses(pathId: StepId, trekId: TrekId) = trekDao.flowPathProgresses(pathId, trekId)
}

private enum class StepStatus {
    Unfinished, // some steps incomplete
    Finished, // all steps completed
    Completed, // all questions answered
}