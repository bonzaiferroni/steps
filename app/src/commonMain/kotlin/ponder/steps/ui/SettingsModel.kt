package ponder.steps.ui

import ponder.steps.model.data.SpeechVoice
import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class SettingsModel(
    private val valueRepo: ValueRepository = LocalValueRepository()
): StateModel<SettingsState>() {
    override val state = ViewState(SettingsState(
        defaultImageTheme = valueRepo.readString(SETTINGS_DEFAULT_IMAGE_THEME),
        defaultAudioTheme = valueRepo.readString(SETTINGS_DEFAULT_AUDIO_THEME),
        defaultVoice = valueRepo.readInt(SETTINGS_DEFAULT_VOICE).let { SpeechVoice.entries[it] }
    ))

    fun setImageTheme(value: String) {
        setState { it.copy(defaultImageTheme = value) }
        valueRepo.writeString(SETTINGS_DEFAULT_IMAGE_THEME, value)
    }

    fun setAudioTheme(value: String) {
        setState { it.copy(defaultAudioTheme = value) }
        valueRepo.writeString(SETTINGS_DEFAULT_AUDIO_THEME, value)
    }

    fun setDefaultVoice(voice: SpeechVoice) {
        setState { it.copy(defaultVoice = voice) }
        valueRepo.writeInt(SETTINGS_DEFAULT_VOICE, voice.ordinal)
    }
}

data class SettingsState(
    val defaultImageTheme: String = "",
    val defaultAudioTheme: String = "",
    val defaultVoice: SpeechVoice = SpeechVoice.entries[0],
)

const val SETTINGS_DEFAULT_IMAGE_THEME = "default_image_theme"
const val SETTINGS_DEFAULT_AUDIO_THEME = "default_audio_theme"
const val SETTINGS_DEFAULT_VOICE = "default_voice"
