package ponder.steps.ui

import pondui.LocalValueRepository
import pondui.ValueRepository
import pondui.ui.core.StateModel

class SettingsModel(
    private val valueRepo: ValueRepository = LocalValueRepository()
): StateModel<SettingsState>(SettingsState(
    defaultTheme = valueRepo.readString(SETTINGS_DEFAULT_THEME)
)) {

    fun setTheme(value: String) {
        setState { it.copy(defaultTheme = value) }
        valueRepo.writeString(SETTINGS_DEFAULT_THEME, value)
    }
}

data class SettingsState(
    val defaultTheme: String = ""
)

const val SETTINGS_DEFAULT_THEME = "default_theme"