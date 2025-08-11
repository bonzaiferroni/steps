package ponder.steps.ui

import ponder.steps.io.AiClient
import kabinet.model.SpeechRequest
import kabinet.model.SpeechVoice
import pondui.LocalValueSource
import pondui.ValueRepository

class SpeechService(
    private val valueRepo: ValueRepository = LocalValueSource(),
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