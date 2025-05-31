package ponder.steps.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import ponder.steps.model.Api
import ponder.steps.server.db.services.IntentionService

fun Routing.serveIntents(service: IntentionService = IntentionService()) {
    get(Api.Intents, { it.toLong()} ) { intentId, endpoint ->
        service.readIntent(intentId)
    }

    authenticateJwt {
        get(Api.Intents.User) {
            val userId = call.getUserId()
            service.readUserIntents(userId)
        }

        post(Api.Intents.Create) { newIntent, endpoint ->
            val userId = call.getUserId()
            service.createIntent(newIntent, userId)
        }

        update(Api.Intents.Update) { intent, endpoint ->
            val userId = call.getUserId()
            if (userId != intent.userId) {
                call.respond(HttpStatusCode.Forbidden)
                null
            } else {
                service.updateIntent(intent, userId)
            }
        }

        delete(Api.Intents.Delete) { intentId, endpoint ->
            val userId = call.getUserId()
            service.deleteIntent(intentId, userId)
        }
    }
}