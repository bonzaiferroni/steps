@file:OptIn(ExperimentalUuidApi::class)

package ponder.steps.io

import ponder.steps.db
import ponder.steps.db.Default
import ponder.steps.db.PathStepEntity
import ponder.steps.db.StepEntity
import ponder.steps.db.StepDao
import ponder.steps.db.toStep
import ponder.steps.db.toStepEntity
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class StepStore(private val dao: StepDao = db.getStepDao()) {
    suspend fun readStep(stepId: String) = dao.readStepOrNull(stepId)?.toStep()

    suspend fun readPath(pathId: String) = dao.readStepOrNull(pathId)?.toStep(
        children = dao.readPathSteps(pathId).map { it.toStep() }
    )

    suspend fun readRootSteps() = dao.readRootSteps().map { it.toStep() }

    suspend fun createStep(newStep: NewStep): String {
        val (pathId, label, position) = newStep
        val id = Uuid.random().toString()
        dao.insert(
            StepEntity.Default.copy(
                id = id,
                label = label
            )
        )

        if (pathId == null || position == null) return id

        val pathStepId = Uuid.random().toString()
        dao.insert(
            PathStepEntity(
                id = pathStepId,
                stepId = id,
                pathId = pathId,
                position = position
            )
        )

        updatePathSize(pathId)

        return id
    }

    suspend fun deleteStep(step: Step): Boolean {
        val pathIds = dao.readPathIdsWithStepId(step.id)
        val isSuccess = dao.deleteStep(step.toStepEntity()) == 1
        if (isSuccess) {
            pathIds.forEach { updatePathSize(it) }
        }
        return isSuccess
    }

    suspend fun updateStep(step: Step) = dao.updateSteps(step.toStepEntity()) == 1

    private suspend fun updatePathSize(pathId: String) {
        val pathSize = dao.readPathStepCount(pathId)
        val path = dao.readStep(pathId).copy(pathSize = pathSize)
        dao.updateSteps(path)
    }
}