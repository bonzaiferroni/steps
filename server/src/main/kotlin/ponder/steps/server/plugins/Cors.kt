package ponder.steps.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)  // Allow Authorization header
        // anyHost()  // Don't use this in production, specify the exact domain(s)
        allowHost("localhost:9000")
        allowHost("192.168.1.100:9000")
        allowHost("localhost:8080")
        allowHost("192.168.1.122:8080")
        allowHost("streetlight.ing")
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
}