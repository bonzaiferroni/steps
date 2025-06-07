package ponder.steps.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.rowset.internal.Row
import pondui.ui.controls.Button
import pondui.ui.controls.ControlSet
import pondui.ui.controls.FlowRow
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.TitleCloud
import pondui.ui.controls.actionable

@Composable
fun TodoView() {
    val viewModel = viewModel { TodoModel() }
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose(viewModel::onDispose)
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad()
    }

    TitleCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        onDismiss = viewModel::toggleAddItem
    ) {
        ControlSet {
            TextField(state.newStepLabel, viewModel::setNewStepLabel)
            Button("Create", onClick = viewModel::createStep)
        }
        LazyColumn(1) {
            items(state.searchedSteps) { step ->
                StepItem(step, modifier = Modifier.actionable { viewModel.addSearchedStep(step) })
            }
        }
    }

    LazyColumn(1) {
        items(state.items, key = { it.trekId }) { item ->
            FlowRow(1) {
                Text(item.stepLabel)
            }
        }
        item {
            Button("Add", onClick = viewModel::toggleAddItem)
        }
    }
}