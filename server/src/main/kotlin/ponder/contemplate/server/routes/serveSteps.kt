package ponder.contemplate.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import ponder.contemplate.model.Api
import ponder.contemplate.server.db.services.StepApiService

fun Routing.serveSteps(service: StepApiService = StepApiService()) {
    // Ahoy! This route be for fetchin' a single step by its id!
    getById(Api.Steps) { stepId, endpoint ->
        service.readStep(stepId.toInt())
    }

    authenticateJwt {
        // Gather all steps for a parent, like collectin' all pieces of a treasure map!
        get(Api.Steps.Parent) {
            val parentId = call.getIdOrThrow { it.toIntOrNull() }
            service.readStepsByParent(parentId)
        }

        // Arr! Fetch all the root steps - the ones with no parent, like the captain of a ship with no superior!
        get(Api.Steps.Root) {
            service.readRootSteps()
        }

        // Add a new step to the plan, like markin' a new X on yer treasure map!
        post(Api.Steps.Create) { newStep, endpoint ->
            service.createStep(newStep)
        }

        // Update a step, like redrawin' part of yer map when ye find better information!
        update(Api.Steps.Update) { step, endpoint ->
            service.updateStep(step)
        }

        // Remove a step from the plan, like crossin' out a spot on yer map that turned out to be empty!
        delete(Api.Steps.Delete) { stepId, endpoint ->
            service.deleteStep(stepId.toInt())
        }
    }
}
