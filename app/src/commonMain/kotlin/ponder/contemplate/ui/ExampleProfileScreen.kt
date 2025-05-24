package ponder.contemplate.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.contemplate.ExampleProfileRoute
import pondui.ui.controls.Button
import pondui.ui.controls.Controls
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.nav.Scaffold
import pondui.ui.theme.Pond

@Composable
fun ExampleProfileScreen(
    route: ExampleProfileRoute,
    viewModel: ExampleProfileModel = viewModel { ExampleProfileModel(route) }
) {
    val state by viewModel.state.collectAsState()
    val example = state.example
    if (example == null) return
    Scaffold {
        Controls {
            if (state.isEditing) {
                TextField(state.symtrix, onTextChange = viewModel::setSymtrix)
                Button("Done", onClick = viewModel::finalizeEdit)
            } else {
                Text(example.symtrix)
                Button("Edit", onClick = viewModel::toggleEdit, background = Pond.colors.secondary)
            }
        }
    }
}