package ponder.steps.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.steps.ExampleProfileRoute
import pondui.ui.controls.*
import pondui.ui.nav.Scaffold
import pondui.ui.theme.Pond
import pondui.ui.theme.Spacing

@Composable
fun ExampleListScreen() {
    val viewModel: ExampleListModel = viewModel { ExampleListModel() }
    val state by viewModel.state.collectAsState()

    Cloud(state.isCreatingItem, viewModel::toggleIsCreatingItem) {
        ControlSet {
            TextField(state.newLabel, viewModel::setLabel)
            ControlSetButton("Add", onClick = viewModel::createNewItem)
        }
    }

    Scaffold {
        LazyColumn(Spacing.Unit) {
            items(state.examples) {
                Row(1) {
                    Text(it.label)
                    Expando()
                    ControlSet {
                        DangerButton("Delete") { viewModel.deleteItem(it) }
                        RouteButton("View") { ExampleProfileRoute(it.id) }
                    }
                }
            }
        }
        Button("Create", onClick = viewModel::toggleIsCreatingItem)
    }
}