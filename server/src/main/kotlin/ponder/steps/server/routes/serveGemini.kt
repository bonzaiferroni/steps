package ponder.steps.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import ponder.steps.model.Api
import ponder.steps.server.clients.GeminiService

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
}