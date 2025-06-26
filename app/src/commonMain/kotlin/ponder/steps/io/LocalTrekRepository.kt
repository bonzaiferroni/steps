package ponder.steps.io

import kabinet.utils.randomUuidStringId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.appUserId
import ponder.steps.db.AnswerDao
import ponder.steps.db.IntentDao
import ponder.steps.db.StepLogDao
import ponder.steps.db.StepLogEntity
import ponder.steps.db.PathStepDao
import ponder.steps.db.QuestionDao
import ponder.steps.db.StepDao
import ponder.steps.db.TrekDao
import ponder.steps.db.TrekEntity
import ponder.steps.db.toEntity
import ponder.steps.model.data.Answer
import ponder.steps.model.data.Intent
import ponder.steps.model.data.IntentTiming
import ponder.steps.model.data.PathStep
import ponder.steps.model.data.PathStepId
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

    suspend fun startTrek(trekId: String): Boolean {
        var trek = trekDao.readTrekById(trekId) ?: return false
        trek = trek.copy(startedAt = Clock.System.now())
        return trekDao.update(trek.toEntity()) == 1
    }

    suspend fun pauseTrek(trekId: String): Boolean {
        var trek = trekDao.readTrekById(trekId) ?: return false
        trek = trek.copy(startedAt = null)
        return trekDao.update(trek.toEntity()) == 1
    }

    override suspend fun syncTreksWithIntents() {
        val activeIntentIds = intentDao.readActiveIntentIds()
        val trekIntentIds = trekDao.readActiveTrekIntentIds()

        for (intentId in activeIntentIds - trekIntentIds) {
            val intent = intentDao.readIntentById(intentId)
            val repeatMins = intent.repeatMins
            val lastAvailableAt = trekDao.readLastAvailableAt(intent.id)
            val lastFinishedAt = trekDao.readLastFinishedAt(intent.id)
            if (lastAvailableAt != null && intent.timing != IntentTiming.Repeat) continue
            val now = Clock.System.now()
            val scheduledAt = intent.scheduledAt ?: Instant.DISTANT_FUTURE
            if (intent.timing == IntentTiming.Schedule && scheduledAt > now) continue

            val availableAt = if (intent.timing != IntentTiming.Repeat || repeatMins == null || lastFinishedAt == null)
                (intent.scheduledAt ?: now) else lastFinishedAt + repeatMins.minutes

            val id = randomUuidStringId()

            trekDao.create(
                TrekEntity(
                    id = id,
                    userId = appUserId,
                    intentId = intentId,
                    superId = null,
                    pathStepId = null,
                    rootId = intent.rootId,
                    progress = 0,
                    isComplete = false,
                    availableAt = availableAt,
                    startedAt = null,
                    progressAt = null,
                    finishedAt = null,
                    expectedAt = intent.expectedMins?.let { mins -> Clock.System.now() + mins.minutes }
                ))
        }
    }

    override suspend fun setOutcome(
        trekId: TrekId,
        stepId: StepId,
        pathStepId: PathStepId?,
        outcome: StepOutcome?
    ): StepLogId? {
        val now = Clock.System.now()
        val logId = if (outcome != null) {
            val logId = randomUuidStringId()
            stepLogDao.insert(
                StepLogEntity(
                    id = logId,
                    stepId = stepId,
                    trekId = trekId,
                    pathStepId = pathStepId,
                    outcome = outcome,
                    updatedAt = now,
                    createdAt = now
                )
            )
            logId
        } else {
            if (pathStepId != null) {
                stepLogDao.delete(trekId, stepId, pathStepId)
            } else {
                stepLogDao.deleteIfNullPathStepId(trekId, stepId)
            }
            null
        }

        setProgress(trekId)

        return logId
    }

    override suspend fun createSubTrek(trekId: String, pathStepId: String): String {
        val trek = trekDao.readTrekById(trekId) ?: error("No trek with id: $trekId")
        val pathStep = pathStepDao.readPathStep(pathStepId) ?: error("No pathstep with id: $pathStepId")
        val id = randomUuidStringId()
        val rootId = pathStep.stepId
        val subTrek = Trek(
            id = id,
            userId = appUserId,
            intentId = trek.intentId,
            superId = trek.id,
            pathStepId = pathStepId,
            rootId = rootId,
            progress = 0,
            isComplete = false,
            availableAt = trek.availableAt,
            startedAt = null,
            progressAt = null,
            finishedAt = null,
            expectedAt = null,
        )

        trekDao.create(subTrek.toEntity())
        return id
    }

    override suspend fun completeTrek(trekId: String): Boolean {
        var trek = trekDao.readTrekById(trekId) ?: return false
        val intent = intentDao.readIntentById(trek.intentId)
        if (intent.repeatMins == null) {
            intentDao.update(
                intent.copy(
                    completedAt = trek.finishedAt
                ).toEntity()
            )
        }
        return trekDao.update(
            trek.copy(
                isComplete = true
            ).toEntity()
        ) == 1
    }

    override suspend fun isFinished(trekId: String) = trekDao.isFinished(trekId)

    private suspend fun resolveAvailableAtFromLastTrek(intent: Intent): Instant? {
        val repeatMins = intent.repeatMins ?: return null
        val lastAvailableAt = trekDao.readLastAvailableAt(intent.id) ?: return null
        return lastAvailableAt + repeatMins.minutes
    }

    override fun flowTrekStepById(trekId: String) = trekDao.flowTrekStepById(trekId)

    override fun flowTrekStepsBySuperId(superId: String) = trekDao.flowTrekStepsBySuperId(superId)

    override fun flowRootTrekSteps(start: Instant, end: Instant) = trekDao.flowRootTrekSteps(start, end)

    suspend fun setProgress(trekId: TrekId?) {
        val trekId = trekId ?: return
        val trek = trekDao.readTrekById(trekId) ?: return
        val pathSteps = pathStepDao.readPathStepsByPathId(trek.rootId)
        val logs = stepLogDao.readStepLogsByTrekId(trekId)
        val status = getStatus(trek, pathSteps, logs)

        trekDao.update(
            when (status) {
                TrekStatus.Unfinished -> trek.copy(
                    progress = logs.size,
                    isComplete = false,
                    finishedAt = null
                )

                TrekStatus.Finished -> trek.copy(
                    progress = logs.size,
                    isComplete = false,
                    finishedAt = trek.finishedAt ?: Clock.System.now()
                )

                TrekStatus.Completed -> trek.copy(
                    progress = logs.size,
                    isComplete = true,
                    finishedAt = trek.finishedAt ?: Clock.System.now()
                )
            }.toEntity()
        )

        val pathStepId = trek.pathStepId
        val superId = trek.superId
        if (superId != null && pathStepId != null) {
            if (status == TrekStatus.Unfinished) {
                stepLogDao.delete(superId, trek.rootId, pathStepId)
            } else {
                val logId = randomUuidStringId()
                val now = Clock.System.now()
                stepLogDao.insert(
                    StepLogEntity(
                        id = logId,
                        stepId = trek.rootId,
                        trekId = superId,
                        pathStepId = pathStepId,
                        outcome = StepOutcome.Completed,
                        updatedAt = now,
                        createdAt = now
                    )
                )
            }
        }

        setProgress(trek.superId) // recursion
    }

    private suspend fun getStatus(
        trek: Trek,
        pathSteps: List<PathStep>,
        logs: List<StepLog>,
    ): TrekStatus {
        if (pathSteps.isEmpty()) {
            val trekStepLog = logs.firstOrNull { it.stepId == trek.rootId } ?: return TrekStatus.Unfinished
            if (trekStepLog.outcome == StepOutcome.Skipped) return TrekStatus.Completed
            val questions = questionDao.readQuestionsByStepId(trek.rootId)
            if (questions.isEmpty()) return TrekStatus.Completed
            val answers = answerDao.readAnswersByLogIds(logs.map { it.id })
            if (answers.any { it.logId == trekStepLog.id }) return TrekStatus.Completed
            else return TrekStatus.Finished
        }
        if (pathSteps.any { ps -> logs.all { l -> l.pathStepId != ps.id } }) return TrekStatus.Unfinished
        val unskippedStepIds =
            pathSteps.mapNotNull { ps -> if (logs.all { l -> l.outcome != StepOutcome.Skipped }) ps.stepId else null }
        val questions = questionDao.readQuestionsByStepIds(unskippedStepIds)
        val answers = answerDao.readAnswersByLogIds(logs.map { it.id })
        if (questions.all { q -> answers.any { a -> a.questionId == q.id } }) return TrekStatus.Completed
        else return TrekStatus.Finished
    }

    override suspend fun createAnswer(trekId: TrekId, answer: Answer): Boolean {
        val isSuccess = answerDao.insert(answer.toEntity()) != -1L
        if (isSuccess) {
            setProgress(trekId)
        }
        return isSuccess
    }
}

private enum class TrekStatus {
    Unfinished, // some steps incomplete
    Finished, // all steps completed
    Completed, // all questions answered
}