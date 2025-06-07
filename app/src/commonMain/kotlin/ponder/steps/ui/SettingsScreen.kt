package ponder.steps.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.EditText
import pondui.ui.controls.Label
import pondui.ui.controls.Scaffold
import pondui.ui.theme.Pond

@Composable
fun SettingsScreen() {
    val viewModel = viewModel { SettingsModel() }
    val state by viewModel.state.collectAsState()

    Scaffold {
        Label("Default image theme")
        EditText(
            text = state.defaultTheme,
            placeholder = "Set theme",
            onAcceptEdit = viewModel::setTheme,
            modifier = Modifier.padding(horizontal = Pond.ruler.unitSpacing * 6)
        )
    }
}