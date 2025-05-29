package ponder.steps.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import ponder.steps.model.Api
import ponder.steps.server.clients.GeminiService
import java.io.File
import java.util.Base64

/**
 * Arr! This be the route for the Gemini AI chat, ye scallywags!
 * It takes a list of messages and returns the AI's response.
 */
fun Routing.serveGemini(service: GeminiService = GeminiService()) {
    // No need for authentication for this endpoint
    post(Api.Gemini.Chat) { messages, endpoint ->
        // Send the messages to the AI and get a response
        service.chat(messages)
    }

    post(Api.Gemini.Image) { request, endpoint ->
        service.generateImage(request)
    }
}

fun requestToFilename(input: String): String =
    input
        .take(64)
        .replace(Regex("[^A-Za-z0-9]"), "_")
