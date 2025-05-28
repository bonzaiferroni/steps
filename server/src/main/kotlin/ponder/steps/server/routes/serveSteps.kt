package ponder.steps.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import ponder.steps.model.Api
import ponder.steps.server.db.services.StepApiService

fun Routing.serveSteps(service: StepApiService = StepApiService()) {
    // Ahoy! This route be for fetchin' a single step by its id!
    getById(Api.Steps, { it }) { stepId, endpoint ->
        service.readStep(stepId)
    }

    authenticateJwt {
        get(Api.Steps.Parent) {
            val parentId = call.getIdOrThrow { it }
            service.readStepsByParent(parentId)
        }

        get(Api.Steps.Root) {
            service.readRootSteps()
        }

        post(Api.Steps.Create) { newStep, endpoint ->
            service.createStep(newStep)
        }

        update(Api.Steps.Update) { step, endpoint ->
            service.updateStep(step)
        }

        delete(Api.Steps.Delete) { stepId, endpoint ->
            service.deleteStep(stepId)
        }
    }
}
