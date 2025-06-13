@file:OptIn(ExperimentalUuidApi::class)

package ponder.steps.io

import kabinet.utils.randomUuidStringId
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.appUserId
import ponder.steps.db.PathStepDao
import ponder.steps.db.PathStepEntity
import ponder.steps.db.StepEntity
import ponder.steps.db.StepDao
import ponder.steps.db.toEntity
import ponder.steps.db.toStep
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import ponder.steps.model.data.SyncData
import kotlin.uuid.ExperimentalUuidApi

class LocalStepRepository(
    private val stepDao: StepDao = appDb.getStepDao(),
    private val pathStepDao: PathStepDao = appDb.getPathStepDao(),
    private val userId: String = appUserId
) : StepRepository {

    override suspend fun createStep(newStep: NewStep): String {
        val (label, pathId, position) = newStep
        val stepId = randomUuidStringId()
        stepDao.insert(
            StepEntity.Empty.copy(
                id = stepId,
                label = label,
                userId = userId,
                description = newStep.description
            )
        )

        if (pathId == null) return stepId

        addVerifiedStepToPath(pathId, stepId, position)

        return stepId
    }

    override suspend fun updateStep(step: Step) = stepDao.update(step.toEntity()) == 1

    override suspend fun removeStepFromPath(pathId: String, stepId: String, position: Int): Boolean {
        val pathStep = pathStepDao.readPathStepAtPosition(pathId, stepId, position) ?: return false
        return pathStepDao.deletePathStep(pathStep.toEntity()) == 1
    }

    override suspend fun addStepToPath(pathId: String, stepId: String, position: Int?): Boolean {
        // make sure the step is not upstream to avoid recursion
        var upstreamIds = listOf(pathId)
        while (upstreamIds.isNotEmpty()) {
            if (upstreamIds.contains(stepId)) return false
            upstreamIds = stepDao.readPathIds(upstreamIds)
        }
        addVerifiedStepToPath(pathId, stepId, position)
        return true
    }

    private suspend fun addVerifiedStepToPath(pathId: String, stepId: String, position: Int?) {
        val pathPosition = position ?: stepDao.readFinalPosition(pathId)?.let { it + 1 } ?: 0

        val pathStepId = randomUuidStringId()
        pathStepDao.insert(
            PathStepEntity(
                id = pathStepId,
                stepId = stepId,
                pathId = pathId,
                position = pathPosition
            )
        )

        updatePathSize(pathId)
    }

    override suspend fun deleteStep(stepId: String): Boolean {
        val pathIds = stepDao.readPathIdsWithStepId(stepId)
        val isSuccess = stepDao.deleteStepById(stepId) == 1
        if (isSuccess) {
            pathIds.forEach { updatePathSize(it) }
        }
        return isSuccess
    }

    private suspend fun updatePathSize(pathId: String) {
        val pathSize = pathStepDao.readPathStepCount(pathId)
        val path = stepDao.readStep(pathId).copy(pathSize = pathSize)
        stepDao.update(path)
    }

    override suspend fun readStep(stepId: String) = stepDao.readStepOrNull(stepId)?.toStep()

    override fun flowStep(stepId: String) = stepDao.flowStep(stepId).map { it.toStep() }

    override suspend fun readPathSteps(pathId: String) = pathStepDao.readPathSteps(pathId = pathId).map { it.toStep() }

    override fun flowPathSteps(pathId: String) = pathStepDao.flowPathSteps(pathId).map { list -> list.map { it.toStep() } }

    override suspend fun readRootSteps(limit: Int) = stepDao.readRootSteps(limit).map { it.toStep() }

    override fun flowRootSteps(limit: Int) = stepDao.flowRootSteps(limit).map { list -> list.map { it.toStep() } }

    override suspend fun moveStepPosition(pathId: String, stepId: String, delta: Int): Boolean {
        if (delta == 0) return false
        val pathStep = pathStepDao.readPathStep(pathId, stepId)
        val movedPosition = pathStep.position + delta
        if (movedPosition < 0) return false
        val stepCount = pathStepDao.readPathStepCount(pathId)
        if (movedPosition > stepCount - 1) return false
        val displacedPathStep = pathStepDao.readPathStepByPosition(pathId, movedPosition) ?: return false
        return pathStepDao.update(
            PathStepEntity(
                id = pathStep.id,
                stepId = pathStep.stepId,
                pathId = pathStep.pathId,
                position = displacedPathStep.position
            ),
            PathStepEntity(
                id = displacedPathStep.id,
                stepId = displacedPathStep.stepId,
                pathId = displacedPathStep.pathId,
                position = pathStep.position
            )
        ) == 2
    }

    override suspend fun readSearch(text: String, limit: Int) = stepDao.searchSteps(text, limit).map { it.toStep() }

    override fun flowSearch(text: String, limit: Int) = stepDao.flowSearch(text, limit).map { list -> list.map { it.toStep() } }

    override suspend fun readSync(lastSyncAt: Instant): SyncData {
        val steps = stepDao.readStepsUpdatedAfter(lastSyncAt).map { it.toStep() }
        val pathSteps = pathStepDao.readPathStepsByPathIds(steps.map { it.id })
        return SyncData(steps, pathSteps)
    }

    override suspend fun writeSync(data: SyncData): Int {
        val count = stepDao.upsert(*data.steps.map { it.toEntity() }.toTypedArray()).size
        pathStepDao.upsert(*data.pathSteps.map { it.toEntity() }.toTypedArray())
        return count
    }
}