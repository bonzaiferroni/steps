package ponder.steps.io

import kabinet.clients.GeminiMessage
import ponder.steps.model.Api
import pondui.io.ApiClient
import pondui.io.globalApiClient

/**
 * Arr! This be the store for talkin' to the Gemini AI, ye scallywag!
 * It sends yer messages to the server and brings back the AI's response.
 */
class GeminiRepository(private val client: ApiClient = globalApiClient) {
    /**
     * Send a list o' messages to the AI and get back a response, savvy?
     */
    suspend fun chat(messages: List<GeminiMessage>) = client.post(Api.Gemini.Chat, messages)

    suspend fun image(request: String) = client.post(Api.Gemini.Image, request)

}
