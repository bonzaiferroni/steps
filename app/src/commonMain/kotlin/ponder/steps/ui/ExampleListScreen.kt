package ponder.steps.ui

import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.steps.ExampleProfileRoute
import pondui.ui.controls.*
import pondui.ui.controls.Scaffold
import pondui.ui.theme.Pond

@Composable
fun ExampleListScreen() {
    val viewModel: ExampleListModel = viewModel { ExampleListModel() }
    val state by viewModel.state.collectAsState()

    Cloud(state.isCreatingItem, viewModel::toggleIsCreatingItem) {
        ControlSet {
            TextField(state.newLabel, onTextChanged = viewModel::setLabel)
            ControlSetButton("Add", onClick = viewModel::createNewItem)
        }
    }

    Scaffold {
        LazyColumn(1) {
            items(state.examples) {
                Row(1) {
                    Text(it.label)
                    Expando()
                    ControlSet {
                        Button("Delete", background = Pond.colors.danger) { viewModel.deleteItem(it) }
                        RouteButton("View") { ExampleProfileRoute(it.id) }
                    }
                }
            }
        }
        Button("Create", onClick = viewModel::toggleIsCreatingItem)
    }
}