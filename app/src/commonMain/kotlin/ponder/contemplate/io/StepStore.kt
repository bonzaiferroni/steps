package ponder.contemplate.io

import ponder.contemplate.model.Api
import ponder.contemplate.model.data.Step
import ponder.contemplate.model.data.NewStep
import pondui.io.ApiStore

class StepStore: ApiStore() {
    // Fetch a single step by its id, like finding a specific spot on yer treasure map!
    suspend fun readStep(stepId: Int) = client.get(Api.Steps, stepId)

    // Fetch all steps for a parent, like gathering all the clues that lead to the treasure!
    suspend fun readStepsByParent(parentId: Int) = client.get(Api.Steps.Parent, "id" to parentId.toString())

    // Arr! Fetch all the root steps - the ones with no parent, like the captain of a ship with no superior!
    suspend fun readRootSteps() = client.get(Api.Steps.Root)

    // Add a new step to the plan, like marking a new X on yer map!
    suspend fun createStep(newStep: NewStep) = client.post(Api.Steps.Create, newStep)

    // Update a step, like correcting the coordinates on yer map when ye get better bearings!
    suspend fun updateStep(step: Step) = client.update(Api.Steps.Update, step)

    // Remove a step from the plan, like crossing out a false lead on yer treasure hunt!
    suspend fun deleteStep(stepId: Int) = client.delete(Api.Steps.Delete, stepId)
}
