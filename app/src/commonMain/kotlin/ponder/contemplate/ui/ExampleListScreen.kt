package ponder.contemplate.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.contemplate.ExampleProfileRoute
import pondui.ui.controls.Button
import pondui.ui.controls.Controls
import pondui.ui.controls.RouteButton
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.nav.Scaffold
import pondui.ui.theme.Pond

@Composable
fun ExampleListScreen(
    viewModel: ExampleListModel = viewModel { ExampleListModel() }
) {
    val state by viewModel.state.collectAsState()
    Scaffold {
        Controls {
            TextField(state.newLabel, viewModel::setLabel)
            Button("Add", onClick = viewModel::createNewItem)
        }
        LazyColumn {
            items(state.examples) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(it.symtrix)
                    Controls {
                        Button("Delete", background = Pond.colors.secondary, onClick = { viewModel.deleteItem(it) })
                        RouteButton("View") { ExampleProfileRoute(it.id) }
                    }
                }
            }
        }
    }
}