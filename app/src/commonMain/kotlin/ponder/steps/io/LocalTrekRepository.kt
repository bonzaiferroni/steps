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
import ponder.steps.db.TrekPointDao
import ponder.steps.db.TrekPointId
import ponder.steps.db.toEntity
import ponder.steps.model.data.Intent
import ponder.steps.model.data.IntentId
import ponder.steps.model.data.NewAnswer
import ponder.steps.model.data.PathStep
import ponder.steps.model.data.Question
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepStatus
import ponder.steps.model.data.Trek
import ponder.steps.model.data.TrekId
import kotlin.time.Duration.Companion.minutes

class LocalTrekRepository(
    private val trekDao: TrekDao = appDb.getTrekDao(),
    private val stepDao: StepDao = appDb.getStepDao(),
    private val pathStepDao: PathStepDao = appDb.getPathStepDao(),
    private val intentDao: IntentDao = appDb.getIntentDao(),
    private val stepLogDao: StepLogDao = appDb.getStepLogDao(),
    private val questionDao: QuestionDao = appDb.getQuestionDao(),
    private val answerDao: AnswerDao = appDb.getAnswerDao(),
    private val trekPointDao: TrekPointDao = appDb.getTrekPointDao(),
) : TrekRepository {

    suspend fun setFinished(
        trekPointId: TrekPointId,
        step: Step,
        outcome: StepOutcome?,
        breadcrumbs: List<Step>?
    ) {
        val trekId = readOrCreateTrekId(trekPointId)
        if (outcome != null) {
            setOutcome(trekId, step, outcome)
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
            isFinished = outcome != null
        )
    }

    private suspend fun setOutcome(trekId: TrekId, step: Step, outcome: StepOutcome) {
        val now = Clock.System.now()
        val status = if (outcome == StepOutcome.Finished) {
            val questionCount = questionDao.countQuestionsByStepId(step.id)
            if (questionCount > 0) StepStatus.AskedQuestion else StepStatus.Completed
        } else {
            StepStatus.Skipped
        }
        stepLogDao.insert(
            StepLogEntity(
                id = randomUuidStringId(),
                stepId = step.id,
                trekId = trekId,
                pathStepId = step.pathStepId,
                status = status,
                updatedAt = now,
                createdAt = now
            )
        )
    }

    private suspend fun readOrCreateTrekId(trekPointId: TrekPointId): TrekId {
        var trekId = trekDao.readTrekIdByTrekPointId(trekPointId)
        if (trekId != null) return trekId

        val intent = intentDao.readIntentByTrekPointId(trekPointId)
        trekId = createTrekFromIntent(intent)
        trekPointDao.updateTrekPointWithTrekId(trekPointId, trekId)
        return trekId
    }

    suspend fun setPathOutcomes(
        trekId: TrekId,
        step: Step,
        breadcrumbs: List<Step>?,
        isFinished: Boolean
    ) {
        val trek = trekDao.readTrekById(trekId) ?: error("trekId not found")

        val isTopLevel = breadcrumbs == null
        val breadcrumbs = breadcrumbs ?: listOf(step)

        var status: PathStatus = PathStatus.Unfinished
        if (isFinished) {
            // mark as complete up the chain of breadcrumbs
            for (step in breadcrumbs.reversed()) {
                status = getPathStatus(trek, step, isTopLevel)
                if (status != PathStatus.Completed) break
                if (step.pathSize > 0) {
                    setOutcome(trekId, step, StepOutcome.Finished)
                }
            }
        } else {
            // mark as incomplete down the chain of breadcrumbs
            stepLogDao.deletePathLogs(trekId, breadcrumbs.map { it.id })
        }

        val updatedTrek = when (status) {
            PathStatus.Unfinished -> when {
                trek.isComplete || trek.finishedAt != null -> trek.copy(
                    isComplete = false,
                    finishedAt = null
                )
                else -> null
            }

            PathStatus.Finished -> when {
                trek.isComplete || trek.finishedAt == null -> trek.copy(
                    isComplete = false,
                    finishedAt = trek.finishedAt ?: Clock.System.now()
                )
                else -> null
            }

            PathStatus.Completed -> when {
                !trek.isComplete || trek.finishedAt == null -> trek.copy(
                    isComplete = true,
                    finishedAt = trek.finishedAt ?: Clock.System.now()
                )
                else -> null
            }
        }

        updatedTrek?.let {
            trekDao.update(it.toEntity())

            if (trek.isComplete != updatedTrek.isComplete) {
                val intent = intentDao.readIntentById(trek.intentId) ?: error("missing intent by id")
                if (intent.repeatMins == null) {
                    intentDao.updateCompletedAt(trek.intentId, updatedTrek.finishedAt)
                }
            }
        }
    }

    private suspend fun getPathStatus(
        trek: Trek,
        step: Step,
        isTopLevel: Boolean,
    ): PathStatus {
        val pathSteps = pathStepDao.readPathStepsByPathId(step.id)
        val logs = if (isTopLevel) stepLogDao.readTopLevelLog(trek.id, step.id)
        else stepLogDao.readTrekLogsByPathId(trek.id, step.id)
        if (pathSteps.isEmpty()) {
            val trekStepLog = logs.firstOrNull { it.stepId == trek.rootId }
            val status = trekStepLog?.status
            return when {
                status == null -> PathStatus.Unfinished
                status == StepStatus.AskedQuestion -> PathStatus.Finished
                else -> PathStatus.Completed
            }
        }

        if (pathSteps.any { ps -> logs.none { l -> l.pathStepId == ps.id } }) return PathStatus.Unfinished
        if (logs.any { it.status == StepStatus.AskedQuestion }) return PathStatus.Finished
        else return PathStatus.Completed
    }

    suspend fun createAnswer(trekPointId: TrekPointId, step: Step, answer: NewAnswer, breadcrumbs: List<Step>?): Boolean {
        val trekId = trekDao.readTrekIdByTrekPointId(trekPointId) ?: error("Missing TrekId")
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
            val questionCount = questionDao.countQuestionsByStepId(step.id)
            val answerCount = answerDao.countAnswersByStepLogId(answer.stepLogId)
            val status = when {
                questionCount == answerCount -> StepStatus.Completed
                else -> StepStatus.AskedQuestion
            }
            setPathOutcomes(trekId, step, breadcrumbs, true)
            if (status == StepStatus.Completed) {
                stepLogDao.updateStepLogStatus(answer.stepLogId, status)
            }
        }
        return isSuccess
    }

    suspend fun readTreksLastStartedAt(intentIds: List<IntentId>) = trekDao.readTreksLastStartedAt(intentIds)

    private suspend fun createTrekFromIntent(intent: Intent): TrekId {
        val id = randomUuidStringId()
        val now = Clock.System.now()

        trekDao.create(
            TrekEntity(
                id = id,
                userId = appUserId,
                intentId = intent.id,
                rootId = intent.rootId,
                isComplete = false,
                createdAt = now,
                finishedAt = null,
                expectedAt = intent.expectedMins?.let { mins -> Clock.System.now() + mins.minutes },
                updatedAt = now
            ))

        return id
    }

    suspend fun readTrekById(trekId: TrekId) = trekDao.readTrekById(trekId)

    fun flowRootTodoSteps(start: Instant) = trekDao.flowRootTodoSteps(start)

    fun flowPathTodoSteps(trekPointId: TrekPointId, pathId: StepId) = trekDao.flowPathTodoSteps(trekPointId, pathId)

    fun flowRootProgress(start: Instant) = trekDao.flowRootProgresses(start)

    fun flowPathProgresses(pathId: StepId, trekPointId: TrekPointId) = trekDao.flowPathProgresses(pathId, trekPointId)

    suspend fun readTrekIdByTrekPointId(trekPointId: TrekPointId) = trekDao.readTrekIdByTrekPointId(trekPointId)

    suspend fun readNextStep(trekPointId: TrekPointId): NextStep? {
        val trekPoint = trekPointDao.readTrekPointById(trekPointId) ?: error("TrekPoint missing: $trekPointId")
        val intent = intentDao.readIntentById(trekPoint.intentId) ?: error("Intent missing: ${trekPoint.intentId}")
        val trek = trekPoint.trekId?.let { trekDao.readTrekById(it) }
        if (trek?.isComplete == true) return null

        val logs = trekPoint.trekId?.let { stepLogDao.readTrekLogsById(it) } ?: emptyList()
        var nextStep = stepDao.readStepById(intent.rootId) ?: error("Step missing: ${intent.rootId}")

        val breadcrumbs = mutableListOf<Step>()
        var exploreStep: Step? = nextStep
        while (exploreStep != null) {
            val pathSteps = pathStepDao.readJoinedPathSteps(exploreStep.id).sortedBy { it.position }
            exploreStep = pathSteps.firstOrNull { step -> logs.none { it.stepId == step.id && it.isCompleted } }
            if (exploreStep != null) {
                breadcrumbs.add(nextStep)
                nextStep = exploreStep
            }
        }

        val questions = questionDao.readQuestionsByStepId(nextStep.id)
        val log = logs.firstOrNull { it.stepId == nextStep.id && it.pathStepId == nextStep.pathStepId }
        val answers = log?.let { answerDao.readAnswersByLogId(log.id) }
        val question = answers?.let { questions.firstOrNull { q -> answers.none { a -> a.questionId == q.id } } }

        return NextStep(
            trekPointId = trekPointId,
            trek = trek,
            stepLog = log,
            step = nextStep,
            breadcrumbs = breadcrumbs.takeIf { it.isNotEmpty() },
            question = question
        )
    }
}

data class NextStep(
    val trekPointId: Long,
    val trek: Trek?,
    val stepLog: StepLog?,
    val step: Step,
    val breadcrumbs: List<Step>?,
    val question: Question?
)

private enum class PathStatus {
    Unfinished, // some steps incomplete
    Finished, // all steps completed
    Completed, // all questions answered
}

enum class StepOutcome {
    Skipped,
    Finished,
}