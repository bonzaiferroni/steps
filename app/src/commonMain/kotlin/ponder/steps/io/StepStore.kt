package ponder.steps.io

import ponder.steps.model.Api
import ponder.steps.model.data.Step
import ponder.steps.model.data.NewStep
import pondui.io.ApiStore

class StepStore: ApiStore() {
    suspend fun readStep(stepId: Long, includeChildren: Boolean) =
        client.get(Api.Steps, stepId, Api.Steps.includeChildren.write(includeChildren))

    suspend fun readPath(parentId: Long, includeChildren: Boolean) =
        client.get(Api.Steps.Parent, parentId, Api.Steps.Parent.includeChildren.write(includeChildren))

    suspend fun readChildren(parentId: Long, includeChildren: Boolean) =
        client.get(Api.Steps.Children, parentId, Api.Steps.Children.includeChildren.write(includeChildren))

    suspend fun readRootSteps(includeChildren: Boolean) =
        client.get(Api.Steps.Root, Api.Steps.includeChildren.write(includeChildren))

    suspend fun generateImage(stepId: Long) =
        client.get(Api.Steps.GenerateImage, stepId)

    suspend fun createStep(newStep: NewStep) = client.post(Api.Steps.Create, newStep)

    suspend fun updateStep(step: Step) = client.update(Api.Steps.Update, step)

    suspend fun deleteStep(stepId: Long) = client.delete(Api.Steps.Delete, stepId)

    suspend fun searchSteps(query: String, includeChildren: Boolean) =
        client.get(Api.Steps.Search, Api.Steps.Search.query.write(query), Api.Steps.Search.includeChildren.write(includeChildren))
}
