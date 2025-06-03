package ponder.steps.io

import kabinet.utils.randomUuidString
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.appUserId
import ponder.steps.db.IntentDao
import ponder.steps.db.StepDao
import ponder.steps.db.TrekDao
import ponder.steps.db.TrekEntity
import ponder.steps.db.toEntity
import ponder.steps.model.data.Intent
import kotlin.time.Duration.Companion.minutes

class TrekStore(
    private val trekDao: TrekDao = appDb.getTrekDao(),
    private val stepDao: StepDao = appDb.getStepDao(),
    private val intentDao: IntentDao = appDb.getIntentDao(),
) {

    fun readTreksSince(time: Instant) = trekDao.readTrekItemsSince(time)

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

    suspend fun stepIntoPath(trekId: String): Boolean {
        var trek = trekDao.readTrekById(trekId) ?: return false
        val pathSize = stepDao.readPathSize(trek.stepId)
        if (pathSize == 0) error("step is not a path")

        val pathIds = trek.pathIds + trek.stepId
        val (stepId, breadCrumbs) = stepIn(trek.stepId, trek.breadCrumbs, pathIds)

        trek = trek.copy(
            breadCrumbs = breadCrumbs,
            stepId = stepId,
            pathIds = pathIds,
            stepCount = stepDao.readTotalStepCount(pathIds)
        )
        return trekDao.update(trek.toEntity()) == 1
    }

    suspend fun syncTreksWithIntents() {
        val activeIntentIds = intentDao.readActiveItentIds()
        val trekIntentIds = trekDao.readActiveTrekIntentIds()

        for (intentId in activeIntentIds - trekIntentIds) {
            val intent = intentDao.readIntentById(intentId)
            val availableAt = intent.scheduledAt ?: resolveAvailableAtFromLastTrek(intent) ?: Clock.System.now()

            val (stepId, breadCrumbs) = stepIn(intent.rootId, emptyList(), intent.pathIds)
            val id = randomUuidString()
            val stepCount = stepDao.readTotalStepCount(intent.pathIds).takeIf { it > 0 } ?: 1

            trekDao.create(TrekEntity(
                id = id,
                userId = appUserId,
                intentId = intentId,
                rootId = intent.rootId,
                stepId = stepId,
                stepIndex = 0,
                stepCount = stepCount,
                pathIds = intent.pathIds,
                breadCrumbs = breadCrumbs,
                availableAt = availableAt,
                startedAt = null,
                progressAt = null,
                finishedAt = null,
                expectedAt = intent.expectedMins?.let { mins -> Clock.System.now() + mins.minutes }
            ))
        }
    }

    suspend fun completeStep(trekId: String): Boolean {
        var trek = trekDao.readTrekById(trekId) ?: return false

        val pathId = trek.breadCrumbs.lastOrNull()
        if (pathId == null) {
            trek = trek.copy(finishedAt = Clock.System.now())
        } else {
            val pathStep = stepDao.readPathStep(pathId, trek.stepId)
            val nextStep = stepDao.readPathStepByPosition(pathId, pathStep.position + 1)
            if (nextStep == null) {
                // stepping out of the path
                val (nextStepId, breadCrumbs) = stepOut(trek.breadCrumbs)
                if (nextStepId == null) {
                    trek = trek.copy(finishedAt = Clock.System.now(), breadCrumbs = breadCrumbs, stepId = trek.rootId)
                } else {
                    trek = trek.copy(breadCrumbs = breadCrumbs, stepId = nextStepId)
                }
            } else {
                val (stepId, breadCrumbs) = stepIn(nextStep.stepId, trek.breadCrumbs, trek.pathIds)
                trek = trek.copy(stepId = stepId, breadCrumbs = breadCrumbs)
            }
        }

        return trekDao.update(trek.copy(
            stepIndex = trek.stepIndex + 1
        ).toEntity()) == 1
    }

    // returns the next step in the trek that is not consumed as a path and the associated breadcrumbs
    private suspend fun stepIn(stepId: String, providedBreadCrumbs: List<String>, pathIds: List<String>): Pair<String, List<String>> {
        var nextStepId = stepId
        var breadCrumbs = providedBreadCrumbs
        while (pathIds.contains(nextStepId)) {
            val pathId = nextStepId
            nextStepId = stepDao.readPathStepByPosition(nextStepId, 0)?.stepId ?: break
            breadCrumbs = breadCrumbs + pathId
        }
        return nextStepId to breadCrumbs
    }

    // returns the next step above the current path, null if the trek is finished
    private suspend fun stepOut(providedBreadCrumbs: List<String>): Pair<String?, List<String>> {
        if (providedBreadCrumbs.isEmpty()) error("Stepped out of empty breadCrumbs")
        var stepId = providedBreadCrumbs.last()
        var breadCrumbs = providedBreadCrumbs - stepId
        var nextStepId: String? = null
        while (breadCrumbs.isNotEmpty()) {
            val pathId = breadCrumbs.last()
            val position = stepDao.readPathStep(pathId, stepId).position
            nextStepId = stepDao.readPathStepByPosition(pathId, position + 1)?.stepId
            if (nextStepId != null) break
            stepId = breadCrumbs.last()
            breadCrumbs = breadCrumbs - stepId
        }
        return nextStepId to breadCrumbs
    }

    private suspend fun resolveAvailableAtFromLastTrek(intent: Intent): Instant? {
        val repeatMins = intent.repeatMins ?: return null
        val lastAvailableAt = trekDao.readLastAvailableAt(intent.id) ?: return null
        return lastAvailableAt + repeatMins.minutes
    }
}