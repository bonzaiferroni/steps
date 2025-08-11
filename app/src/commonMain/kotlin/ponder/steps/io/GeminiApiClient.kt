package ponder.steps.io

import kabinet.clients.GeminiMessage
import kabinet.gemini.GeminiApi
import kabinet.model.SpeechRequest
import ponder.steps.model.Api
import pondui.io.ApiClient
import pondui.io.globalApiClient

class GeminiApiClient(
    private val geminiApi: GeminiApi,
    private val client: ApiClient = globalApiClient
) {
    /**
     * Send a list o' messages to the AI and get back a response, savvy?
     */
    suspend fun chat(messages: List<GeminiMessage>) = client.post(geminiApi.chat, messages)

    suspend fun image(request: String) = client.post(geminiApi.image, request)

    suspend fun generateSpeech(request: SpeechRequest) = client.post(geminiApi.speech, request)
}
