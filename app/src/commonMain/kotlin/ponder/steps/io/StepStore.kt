@file:OptIn(ExperimentalUuidApi::class)

package ponder.steps.io

import ponder.steps.appDb
import ponder.steps.db.Empty
import ponder.steps.db.PathStepEntity
import ponder.steps.db.StepEntity
import ponder.steps.db.StepDao
import ponder.steps.db.toEntity
import ponder.steps.db.toStep
import ponder.steps.db.toStepEntity
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class StepStore(private val dao: StepDao = appDb.getStepDao()) {
    suspend fun readStep(stepId: String) = dao.readStepOrNull(stepId)?.toStep()

    suspend fun readPath(pathId: String) = dao.readStepOrNull(pathId)?.toStep(
        children = dao.readPathSteps(pathId).map { it.toStep() }
    )

    suspend fun readPathSteps(pathId: String) = dao.readPathSteps(pathId = pathId).map { it.toStep() }

    fun readPathStepsFlow(pathId: String) = dao.readPathStepsFlow(pathId = pathId)

    suspend fun readRootSteps() = dao.readRootSteps().map { it.toStep() }

    suspend fun createStep(newStep: NewStep): String {
        val (pathId, label, position) = newStep
        val stepId = Uuid.random().toString()
        dao.insert(
            StepEntity.Empty.copy(
                id = stepId,
                label = label
            )
        )

        if (pathId == null) return stepId

        addStepToPath(pathId, stepId, position)

        return stepId
    }

    suspend fun deleteStep(step: Step): Boolean {
        val pathIds = dao.readPathIdsWithStepId(step.id)
        val isSuccess = dao.deleteStep(step.toStepEntity()) == 1
        if (isSuccess) {
            pathIds.forEach { updatePathSize(it) }
        }
        return isSuccess
    }

    suspend fun moveStepPosition(pathId: String, stepId: String, delta: Int): Boolean {
        if (delta == 0) return false
        val pathStep = dao.readPathStep(pathId, stepId)
        val movedPosition = pathStep.position + delta
        if (movedPosition < 0) return false
        val stepCount = dao.readPathStepCount(pathId)
        if (movedPosition > stepCount - 1) return false
        val displacedPathStep = dao.readPathStepByPosition(pathId, movedPosition) ?: return false
        return dao.update(
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

    suspend fun updateStep(step: Step) = dao.update(step.toStepEntity()) == 1

    suspend fun searchSteps(text: String) = dao.searchSteps(text).map { it.toStep() }

    private suspend fun updatePathSize(pathId: String) {
        val pathSize = dao.readPathStepCount(pathId)
        val path = dao.readStep(pathId).copy(pathSize = pathSize)
        dao.update(path)
    }

    suspend fun removeStepFromPath(pathId: String, stepId: String, position: Int): Boolean {
        val pathStep = dao.readPathStepAtPosition(pathId, stepId, position) ?: return false
        return dao.deletePathStep(pathStep.toEntity()) == 1
    }

    suspend fun addStepToPathIfNotDownstream(pathId: String, stepId: String, position: Int?): Boolean {
        var upstreamIds = listOf(pathId)
        while (upstreamIds.isNotEmpty()) {
            if (upstreamIds.contains(stepId)) return false
            upstreamIds = dao.readPathIds(upstreamIds)
        }
        addStepToPath(pathId, stepId, position)
        return true
    }

    suspend fun addStepToPath(pathId: String, stepId: String, position: Int?) {
        val pathPosition = position ?: dao.readFinalPosition(pathId)?.let { it + 1 } ?: 0

        val pathStepId = Uuid.random().toString()
        dao.insert(
            PathStepEntity(
                id = pathStepId,
                stepId = stepId,
                pathId = pathId,
                position = pathPosition
            )
        )

        updatePathSize(pathId)
    }
}