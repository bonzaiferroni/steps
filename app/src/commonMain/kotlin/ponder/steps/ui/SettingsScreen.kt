package ponder.steps.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.collections.immutable.toImmutableList
import kabinet.model.SpeechVoice
import pondui.ui.controls.EditText
import pondui.ui.controls.Label
import pondui.ui.controls.MenuWheel
import pondui.ui.controls.Scaffold
import pondui.ui.theme.Pond

@Composable
fun SettingsScreen() {
    val viewModel = viewModel { SettingsModel() }
    val state by viewModel.stateFlow.collectAsState()

    Scaffold {
        Label("Default image theme")
        EditText(
            text = state.defaultImageTheme,
            placeholder = "Set theme",
            onAcceptEdit = viewModel::setImageTheme,
            modifier = Modifier.padding(horizontal = Pond.ruler.unitSpacing * 6)
        )
        Label("Default audio theme")
        EditText(
            text = state.defaultAudioTheme,
            placeholder = "Set theme",
            onAcceptEdit = viewModel::setAudioTheme,
            modifier = Modifier.padding(horizontal = Pond.ruler.unitSpacing * 6)
        )
        Label("Default voice:")
        MenuWheel(state.defaultVoice, SpeechVoice.entries.toImmutableList(), onSelect = viewModel::setDefaultVoice)
    }
}