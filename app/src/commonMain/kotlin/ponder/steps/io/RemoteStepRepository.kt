package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import ponder.steps.model.Api
import ponder.steps.model.data.Step
import ponder.steps.model.data.NewStep
import pondui.io.ApiClient
import pondui.io.globalApiClient

class RemoteStepRepository(private val client: ApiClient = globalApiClient): StepRepository {
    override suspend fun createStep(newStep: NewStep) = client.post(Api.Steps.Create, newStep)

    override suspend fun updateStep(step: Step) = client.update(Api.Steps.Update, step)

    override suspend fun addStepToPath(pathId: String, stepId: String, position: Int?): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun removeStepFromPath(pathId: String, stepId: String, position: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun moveStepPosition(pathId: String, stepId: String, delta: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteStep(stepId: String) = client.delete(Api.Steps.Delete, stepId)

    override suspend fun readStep(stepId: String) = client.get(Api.Steps, stepId)
    override fun flowStep(stepId: String): Flow<Step> {
        TODO("Not yet implemented")
    }

    override suspend fun readPathSteps(pathId: String) = client.get(Api.Steps.Children, pathId)
    override fun flowPathSteps(pathId: String): Flow<List<Step>> {
        TODO("Not yet implemented")
    }

    override suspend fun readRootSteps(limit: Int) = client.get(Api.Steps.Root)

    override fun flowRootSteps(limit: Int): Flow<List<Step>> {
        TODO("Not yet implemented")
    }

    override suspend fun readSearch(text: String, limit: Int) = client.get(Api.Steps.Search, Api.Steps.Search.query.write(text))

    override fun flowSearch(
        text: String,
        limit: Int
    ): Flow<List<Step>> {
        TODO("Not yet implemented")
    }

    override suspend fun isValidPathStep(pathId: String, stepId: String): Boolean {
        TODO("Not yet implemented")
    }
}
