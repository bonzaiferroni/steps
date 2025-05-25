package ponder.steps.ui

import kabinet.clients.GeminiClient
import pondui.ui.core.StateModel

/**
 * Arr, this be the model for the Gemini screen, matey!
 * It be holdin' the state and talkin' to the Gemini API.
 */
class GeminiModel(
    private val geminiClient: GeminiClient
): StateModel<GeminiState>(GeminiState()) {
    // Avast! We be leavin' this empty for now, as per the captain's orders!
    // We'll be fillin' it with treasure in the next voyage.
}

/**
 * Shiver me timbers! This be the state for the Gemini screen.
 * Empty as a pirate's rum bottle... for now!
 */
data class GeminiState(
    val placeholder: String = "Ahoy there!"
)