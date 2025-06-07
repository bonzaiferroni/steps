@file:OptIn(ExperimentalUuidApi::class)

package ponder.steps.io

import kabinet.utils.randomUuidStringId
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.appUserId
import ponder.steps.db.PathStepEntity
import ponder.steps.db.StepEntity
import ponder.steps.db.StepDao
import ponder.steps.db.toEntity
import ponder.steps.db.toStep
import ponder.steps.model.data.NewStep
import ponder.steps.model.data.Step
import ponder.steps.model.data.SyncData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class LocalStepRepository(
    private val dao: StepDao = appDb.getStepDao(),
    private val userId: String = appUserId
) : StepRepository {

    override suspend fun createStep(newStep: NewStep): String {
        val (label, pathId, position) = newStep
        val stepId = randomUuidStringId()
        println(stepId)
        dao.insert(
            StepEntity.Empty.copy(
                id = stepId,
                label = label,
                userId = userId
            )
        )

        if (pathId == null) return stepId

        addVerifiedStepToPath(pathId, stepId, position)

        return stepId
    }

    override suspend fun updateStep(step: Step) = dao.update(step.toEntity()) == 1

    override suspend fun removeStepFromPath(pathId: String, stepId: String, position: Int): Boolean {
        val pathStep = dao.readPathStepAtPosition(pathId, stepId, position) ?: return false
        return dao.deletePathStep(pathStep.toEntity()) == 1
    }

    override suspend fun addStepToPath(pathId: String, stepId: String, position: Int?): Boolean {
        // make sure the step is not upstream to avoid recursion
        var upstreamIds = listOf(pathId)
        while (upstreamIds.isNotEmpty()) {
            if (upstreamIds.contains(stepId)) return false
            upstreamIds = dao.readPathIds(upstreamIds)
        }
        addVerifiedStepToPath(pathId, stepId, position)
        return true
    }

    private suspend fun addVerifiedStepToPath(pathId: String, stepId: String, position: Int?) {
        val pathPosition = position ?: dao.readFinalPosition(pathId)?.let { it + 1 } ?: 0

        val pathStepId = randomUuidStringId()
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

    override suspend fun deleteStep(stepId: String): Boolean {
        val pathIds = dao.readPathIdsWithStepId(stepId)
        val isSuccess = dao.deleteStepById(stepId) == 1
        if (isSuccess) {
            pathIds.forEach { updatePathSize(it) }
        }
        return isSuccess
    }

    private suspend fun updatePathSize(pathId: String) {
        val pathSize = dao.readPathStepCount(pathId)
        val path = dao.readStep(pathId).copy(pathSize = pathSize)
        dao.update(path)
    }

    override suspend fun readStep(stepId: String) = dao.readStepOrNull(stepId)?.toStep()

    override suspend fun readPathSteps(pathId: String) = dao.readPathSteps(pathId = pathId).map { it.toStep() }

    override suspend fun readRootSteps() = dao.readRootSteps().map { it.toStep() }

    override suspend fun moveStepPosition(pathId: String, stepId: String, delta: Int): Boolean {
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

    override suspend fun searchSteps(text: String) = dao.searchSteps(text).map { it.toStep() }

    override suspend fun readSync(lastSyncAt: Instant): SyncData {
        val steps = dao.readStepsUpdatedAfter(lastSyncAt).map { it.toStep() }
        val pathSteps = dao.readPathStepsByPathIds(steps.map { it.id })
        return SyncData(steps, pathSteps)
    }

    override suspend fun writeSync(data: SyncData): Int {
        val count = dao.update(*data.steps.map { it.toEntity() }.toTypedArray())
        dao.update(*data.pathSteps.map { it.toEntity() }.toTypedArray())
        return count
    }
}