package ponder.steps.server.routes

import io.ktor.server.routing.Routing
import klutch.server.authenticateJwt
import klutch.server.post
import klutch.utils.getUserId
import ponder.steps.model.Api
import ponder.steps.server.db.services.SyncApiService

fun Routing.serveSync(service: SyncApiService = SyncApiService()) {
    authenticateJwt {
        post(Api.Sync.Read) { request, endpoint ->
            val userId = call.getUserId()
            service.readSync(request.startSyncAt, request.endSyncAt, userId)
        }

        post(Api.Sync.Write) { data, endpoint ->
            val userId = call.getUserId()
            service.writeSync(data, userId)
        }
    }
}
