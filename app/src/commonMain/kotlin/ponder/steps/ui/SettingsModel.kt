package ponder.steps.ui

import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel

class SettingsModel(
    private val valueRepo: ValueRepository = LocalValueRepository()
): StateModel<SettingsState>(SettingsState(
    theme = valueRepo.readString(SETTINGS_KEY_THEME)
)) {

    fun setTheme(value: String) {
        setState { it.copy(theme = value) }
        valueRepo.writeString(SETTINGS_KEY_THEME, value)
    }
}

data class SettingsState(
    val theme: String = ""
)

const val SETTINGS_KEY_THEME = "theme"