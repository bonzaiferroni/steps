package ponder.steps.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
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
        Controls {
            TextField(state.newLabel, viewModel::setLabel)
            Button("Add", onClick = viewModel::createNewItem)
        }
    }

    Scaffold {
        LazyColumn(Spacing.Unit) {
            items(state.examples) {
                Row(Spacing.Unit) {
                    Text(it.label)
                    Controls {
                        Button("Delete", background = Pond.colors.secondary, onClick = { viewModel.deleteItem(it) })
                        RouteButton("View") { ExampleProfileRoute(it.id) }
                    }
                }
            }
        }
        Button("Create", onClick = viewModel::toggleIsCreatingItem)
    }
}