package ponder.steps.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.steps.ExampleProfileRoute
import pondui.ui.controls.Button
import pondui.ui.controls.ControlSet
import pondui.ui.controls.Expando
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.Scaffold
import pondui.ui.theme.Pond

@Composable
fun ExampleProfileScreen(
    route: ExampleProfileRoute,
    viewModel: ExampleProfileModel = viewModel { ExampleProfileModel(route) }
) {
    val state by viewModel.stateFlow.collectAsState()
    val example = state.example
    if (example == null) return
    Scaffold {
        ControlSet {
            if (state.isEditing) {
                TextField(state.label, onTextChanged = viewModel::setLabel)
                Expando()
                Button("Done", onClick = viewModel::finalizeEdit)
            } else {
                Text(example.label)
                Expando()
                Button("Edit", onClick = viewModel::toggleEdit, background = Pond.colors.secondary)
            }
        }
    }
}