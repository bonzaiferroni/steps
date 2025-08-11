package ponder.steps.server.routes

import io.ktor.server.routing.Routing
import kabinet.api.PostEndpoint
import kabinet.clients.GeminiMessage
import kabinet.gemini.GeminiApi
import kabinet.model.ImageUrls
import kabinet.model.SpeechRequest
import klutch.server.*
import ponder.steps.model.Api
import klutch.gemini.GeminiService

/**
 * Arr! This be the route for the Gemini AI chat, ye scallywags!
 * It takes a list of messages and returns the AI's response.
 */
fun Routing.serveGemini(api: GeminiApi, service: GeminiService = GeminiService()) {
    // No need for authentication for this endpoint
    post(api.chat) { messages, endpoint ->
        // Send the messages to the AI and get a response
        service.chat(messages)
    }

    post(api.image) { request, endpoint ->
        service.generateImage(request)
    }

    post(api.speech) { request, endpoint ->
        service.generateSpeech(request)
    }
}