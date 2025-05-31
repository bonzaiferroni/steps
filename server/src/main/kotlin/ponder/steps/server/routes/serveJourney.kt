package ponder.steps.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import ponder.steps.model.Api
import ponder.steps.server.db.services.JourneyService

fun Routing.serveJourney(service: JourneyService = JourneyService()) {
    authenticateJwt {
        get(Api.Journey.UserTreks) {
            val userId = call.getUserId()
            service.readUserTreks(userId)
        }

        post(Api.Journey.CompleteStep) { trekId, endpoint ->
            val userId = call.getUserId()
            service.completeStep(trekId, userId)
            true
        }

        post(Api.Journey.StartTrek) { trekId, endpoint ->
            val userId = call.getUserId()
            service.stepIntoCurrentPath(trekId, userId)
            true
        }
    }
}