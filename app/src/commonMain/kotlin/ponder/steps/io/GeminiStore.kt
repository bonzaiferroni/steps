package ponder.steps.io

import kabinet.clients.GeminiMessage
import ponder.steps.model.Api
import pondui.io.ApiStore

/**
 * Arr! This be the store for talkin' to the Gemini AI, ye scallywag!
 * It sends yer messages to the server and brings back the AI's response.
 */
class GeminiStore : ApiStore() {
    /**
     * Send a list o' messages to the AI and get back a response, savvy?
     */
    suspend fun chat(messages: List<GeminiMessage>): String = client.post(Api.Gemini.Chat, messages)
}
