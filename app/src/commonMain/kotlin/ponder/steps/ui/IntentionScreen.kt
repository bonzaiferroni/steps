package ponder.steps.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.*

@Composable
fun IntentionScreen() {
    val viewModel = viewModel { IntentionModel() }
    val state by viewModel.state.collectAsState()

    TitleCloud("New Intention", state.isAddingItem, viewModel::toggleAddItem) {
        TextField(state.searchPathText, onTextChange = viewModel::setSearchPathText)
        LazyColumn(1) {
            items(state.searchPaths) { step ->
                Button(step.label) { viewModel.createIntent(step.id) }
            }
        }
    }

    Scaffold {
        LazyColumn(1, modifier = Modifier.weight(1f)) {
            items(state.intents, key = { it.id }) { item ->
                FlowRow(1, 2) {
                    H1(item.label)
                }
            }
        }
        Button("Add", onClick = viewModel::toggleAddItem)
    }
}