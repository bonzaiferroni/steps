package ponder.steps.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import ponder.steps.model.Api
import ponder.steps.server.db.services.FocusService
import ponder.steps.server.db.services.TrekApiService

fun Routing.serveJourney(
    service: TrekApiService = TrekApiService(),
    focusService: FocusService = FocusService()
) {
    authenticateJwt {
        get(Api.Journey.UserTreks) {
            val userId = call.getUserId()
            service.readUserTreks(userId)
        }

        post(Api.Journey.CompleteStep) { trekId, endpoint ->
            val userId = call.getUserId()
            service.completeStep(trekId, userId)
        }

        post(Api.Journey.StepIntoPath) { trekId, endpoint ->
            val userId = call.getUserId()
            service.stepIntoPath(trekId, userId)
        }

        get(Api.Journey.FocusTrek) {
            val userId = call.getUserId()
            focusService.readFocus(userId)
        }
    }
}
