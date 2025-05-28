package ponder.steps.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import ponder.steps.model.Api
import ponder.steps.server.db.services.ExampleApiService

fun Routing.serveExamples(service: ExampleApiService = ExampleApiService()) {
    get(Api.Examples, { it.toLong()} ) { exampleId, endpoint ->
        service.readExample(exampleId)
    }

    authenticateJwt {
        get(Api.Examples.User) {
            val userId = call.getUserId()
            service.readUserExamples(userId)
        }

        post(Api.Examples.Create) { newExample, endpoint ->
            val userId = call.getUserId()
            service.createExample(userId, newExample)
        }

        update(Api.Examples.Update) { example, endpoint ->
            val userId = call.getUserId()
            if (userId != example.userId) {
                call.respond(HttpStatusCode.Forbidden)
                null
            } else {
                service.updateExample(example)
            }
        }

        delete(Api.Examples.Delete) { exampleId, endpoint ->
            val userId = call.getUserId()
            service.deleteExample(exampleId, userId)
        }
    }
}