package ponder.steps.ui

import ponder.steps.io.AiClient
import ponder.steps.model.data.SpeechRequest
import ponder.steps.model.data.SpeechVoice
import pondui.LocalValueRepository
import pondui.ValueRepository

class SpeechService(
    private val valueRepo: ValueRepository = LocalValueRepository(),
    private val aiClient: AiClient = AiClient(),
) {
    suspend fun generateSpeech(text: String): String? {
        val request = SpeechRequest(
            text = text,
            theme = valueRepo.readString(SETTINGS_DEFAULT_AUDIO_THEME),
            voice = valueRepo.readInt(SETTINGS_DEFAULT_VOICE).let { SpeechVoice.entries[it] }
        )
        return aiClient.generateSpeech(request)
    }
}