package ponder.steps.io

import kabinet.utils.randomUuidStringId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.appUserId
import ponder.steps.db.IntentDao
import ponder.steps.db.StepLogDao
import ponder.steps.db.StepLogEntity
import ponder.steps.db.PathStepDao
import ponder.steps.db.StepDao
import ponder.steps.db.TrekDao
import ponder.steps.db.TrekEntity
import ponder.steps.db.toEntity
import ponder.steps.model.data.Intent
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.Trek
import kotlin.time.Duration.Companion.minutes

class LocalTrekRepository(
    private val trekDao: TrekDao = appDb.getTrekDao(),
    private val stepDao: StepDao = appDb.getStepDao(),
    private val pathStepDao: PathStepDao = appDb.getPathStepDao(),
    private val intentDao: IntentDao = appDb.getIntentDao(),
    private val stepLogDao: StepLogDao = appDb.getLogDao()
) : TrekRepository {

    override fun flowTreksInRange(start: Instant, end: Instant) = trekDao.flowTreksInRange(start, end)

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
        val activeIntentIds = intentDao.readActiveItentIds()
        val trekIntentIds = trekDao.readActiveTrekIntentIds()

        for (intentId in activeIntentIds - trekIntentIds) {
            val intent = intentDao.readIntentById(intentId)
            val availableAt = intent.scheduledAt ?: resolveAvailableAtFromLastTrek(intent) ?: Clock.System.now()

            val id = randomUuidStringId()
            val nextId = pathStepDao.readPathStepIdByPosition(intent.rootId, 0)

            trekDao.create(
                TrekEntity(
                    id = id,
                    userId = appUserId,
                    intentId = intentId,
                    superId = null,
                    pathStepId = null,
                    rootId = intent.rootId,
                    nextId = nextId,
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
        trekId: String,
        stepId: String,
        pathStepId: String?,
        outcome: StepOutcome?
    ): String? {
        val trek = trekDao.readTrekById(trekId) ?: return null

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
            stepLogDao.delete(stepId, trekId, pathStepId)
            null
        }

        val step = stepDao.readStep(stepId)
        val progress = if (pathStepId == null) 0 else if (outcome == null) trek.progress - 1 else trek.progress + 1
        val finishedAt = if (trek.rootId == stepId || progress == step.pathSize) now else null

        val nextId = if (finishedAt == null) {
            val logs = stepLogDao.readStepLogsByTrekId(trekId)
            pathStepDao.readPathStepsByPathId(trek.rootId).sortedBy { it.position }.firstOrNull { pathStep ->
                logs.all { it.pathStepId != pathStep.id }
            }?.id
        } else null

        trekDao.update(trek.copy(
            progress = progress,
            finishedAt = finishedAt,
            progressAt = now,
            nextId = nextId
        ).toEntity())

        return logId
    }

    override suspend fun createSubTrek(trekId: String, pathStepId: String): String {
        val trek = trekDao.readTrekById(trekId) ?: error("No trek with id: $trekId")
        val pathStep = pathStepDao.readPathStep(pathStepId) ?: error("No pathstep with id: $pathStepId")
        val id = randomUuidStringId()
        val rootId = pathStep.stepId
        val nextId = pathStepDao.readPathStepIdByPosition(rootId, 0)
        val subTrek = Trek(
            id = id,
            userId = appUserId,
            intentId = trek.intentId,
            superId = trek.id,
            pathStepId = pathStepId,
            rootId = rootId,
            nextId = nextId,
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
        return trekDao.update(trek.copy(
            isComplete = true
        ).toEntity()) == 1
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
}