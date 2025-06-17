@file:OptIn(ExperimentalUuidApi::class)

package ponder.steps.io

import kabinet.utils.randomUuidStringId
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
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
                description = newStep.description,
                createdAt = Clock.System.now(),
            )
        )

        if (pathId == null) return stepId

        addVerifiedStepToPath(pathId, stepId, position)

        return stepId
    }

    override suspend fun updateStep(step: Step) = stepDao.update(step.toEntity()) == 1

    override suspend fun removeStepFromPath(pathId: String, stepId: String, position: Int): Boolean {
        val pathStep = pathStepDao.readPathStepAtPosition(pathId, stepId, position) ?: return false
        val isSuccess = pathStepDao.delete(pathStep.toEntity()) == 1
        if (isSuccess) {
            updatePathSize(pathId)
            orderPathSteps(pathId)
        }
        return isSuccess
    }

    override suspend fun isValidPathStep(pathId: String, stepId: String): Boolean {
        var upstreamIds = listOf(pathId)
        while (upstreamIds.isNotEmpty()) {
            if (upstreamIds.contains(stepId)) return false
            upstreamIds = stepDao.readPathIds(upstreamIds)
        }
        return true
    }

    override suspend fun addStepToPath(pathId: String, stepId: String, position: Int?): Boolean {
        // make sure the step is not upstream to avoid recursion
        if (!isValidPathStep(pathId, stepId)) return false
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
                position = pathPosition,
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

    private suspend fun orderPathSteps(pathId: String) {
        val pathSteps = pathStepDao.readPathStepEntities(pathId)
            .sortedBy { it.position }.mapIndexed { index, pathStep ->
                pathStep.copy(position = index)
            }
        pathStepDao.update(*pathSteps.toTypedArray())
    }

    override suspend fun readStep(stepId: String) = stepDao.readStepOrNull(stepId)?.toStep()

    override fun flowStep(stepId: String) = stepDao.flowStep(stepId).map { it.toStep() }

    override suspend fun readPathSteps(pathId: String) = pathStepDao.readJoinedPathSteps(pathId = pathId).map { it.toStep() }

    override fun flowPathSteps(pathId: String) = pathStepDao.flowJoinedSteps(pathId).map { list -> list.map { it.toStep() } }

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
        pathStepDao.setPositions(
            firstId = pathStep.id, firstPos = displacedPathStep.position,
            secondId = displacedPathStep.id, secondPos = pathStep.position,
        )
        return true
    }

    override suspend fun readSearch(text: String, limit: Int) = stepDao.searchSteps(text, limit).map { it.toStep() }

    override fun flowSearch(text: String, limit: Int) = stepDao.flowSearch(text, limit).map { list -> list.map { it.toStep() } }
}