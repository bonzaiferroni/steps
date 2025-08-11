package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.clients.GeminiMessage
import kabinet.clients.GeminiRole
import kotlinx.coroutines.launch
import ponder.steps.io.GeminiApiClient
import ponder.steps.model.geminiApi
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState

/**
 * Arr! This be the model for the Gemini screen, matey!
 * It handles the state and business logic for chattin' with the AI.
 */
class GeminiModel(
    private val geminiStore: GeminiApiClient = GeminiApiClient(geminiApi)
): StateModel<GeminiState>() {
    override val state = ModelState(GeminiState())

    /**
     * Updates the current message text as the user types
     */
    fun updateMessage(message: String) {
        setState { it.copy(message = message) }
    }

    /**
     * Sends the current message to the AI and updates the state with the response
     */
    fun sendMessage() {
        val currentMessage = stateNow.message.trim()
        if (currentMessage.isEmpty()) return

        // Create a new message from the user
        val userMessage = GeminiMessage(
            role = GeminiRole.User,
            message = currentMessage
        )

        // Add the user message to the list and clear the input
        setState { it.copy(
            message = "",
            messages = it.messages + userMessage,
            isLoading = true
        ) }

        // Send the messages to the AI and get a response
        viewModelScope.launch {
            try {
                val allMessages = stateNow.messages
                val response = geminiStore.chat(allMessages) ?: return@launch
                val message = GeminiMessage(GeminiRole.Assistant, response)

                // Add the AI response to the list
                setState { it.copy(
                    messages = it.messages + message,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                // Handle errors
                val errorMessage = GeminiMessage(
                    role = GeminiRole.Assistant,
                    message = "Arr! There be a problem talkin' to the AI: ${e.message}"
                )
                setState { it.copy(
                    messages = it.messages + errorMessage,
                    isLoading = false
                ) }
            }
        }
    }
}

/**
 * Arr! This be the state for the Gemini screen, ye scallywag!
 */
data class GeminiState(
    val message: String = "",
    val messages: List<GeminiMessage> = emptyList(),
    val isLoading: Boolean = false
)
