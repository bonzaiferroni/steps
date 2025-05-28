package ponder.steps.io

import ponder.steps.model.Api
import ponder.steps.model.data.Step
import ponder.steps.model.data.NewStep
import pondui.io.ApiStore

class StepStore: ApiStore() {
    suspend fun readStep(stepId: Int, includeChildren: Boolean) =
        client.get(Api.Steps, stepId, Api.Steps.includeChildren.write(includeChildren))

    suspend fun readStepsByParent(parentId: Int, includeChildren: Boolean) =
        client.get(Api.Steps.Parent, parentId, Api.Steps.Parent.includeChildren.write(includeChildren))

    suspend fun readRootSteps(includeChildren: Boolean) =
        client.get(Api.Steps.Root, Api.Steps.includeChildren.write(includeChildren))

    suspend fun createStep(newStep: NewStep) = client.post(Api.Steps.Create, newStep)

    suspend fun updateStep(step: Step) = client.update(Api.Steps.Update, step)

    suspend fun deleteStep(stepId: String) = client.delete(Api.Steps.Delete, stepId)
}
