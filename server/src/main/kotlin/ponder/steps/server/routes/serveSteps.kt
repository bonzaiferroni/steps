package ponder.steps.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import ponder.steps.model.Api
import ponder.steps.server.db.services.StepApiService

fun Routing.serveSteps(service: StepApiService = StepApiService()) {
    // Ahoy! This route be for fetchin' a single step by its id!
    get(Api.Steps, { it }) { stepId, endpoint ->
        val includeChildren = endpoint.includeChildren.readParam(call)
        service.readStep(stepId, includeChildren)
    }

    authenticateJwt {
        get(Api.Steps.Parent, { it }) { id, endpoint ->
            val includeChildren = endpoint.includeChildren.readParam(call)
            service.readParent(id, includeChildren)
        }

        get(Api.Steps.Children, { it }) { id, endpoint ->
            val parentId = call.getIdOrThrow { it }
            val includeChildren = endpoint.includeChildren.readParam(call)
            service.readChildren(parentId, includeChildren)
        }

        get(Api.Steps.Root) { endpoint ->
            val includeChildren = endpoint.includeChildren.readParam(call)
            service.readRootSteps(includeChildren)
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
