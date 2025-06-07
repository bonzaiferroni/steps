package ponder.steps.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.rowset.internal.Row
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.ControlSet
import pondui.ui.controls.ControlSetButton
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
        Column(1, modifier = Modifier.height(400.dp)) {
            ControlSet {
                TextField(state.newStepLabel, viewModel::setNewStepLabel)
                ControlSetButton("Create", onClick = viewModel::createStep)
            }
            LazyColumn(1) {
                items(state.searchedSteps) { step ->
                    StepItem(step, modifier = Modifier.actionable { viewModel.addSearchedStep(step) })
                }
            }
        }
    }

    LazyColumn(1) {
        items(state.items, key = { it.trekId }) { item ->
            FlowRow(1) {
                StepItem(item.stepLabel, item.stepThumbUrl)
            }
        }
        item {
            Button("Add", onClick = viewModel::toggleAddItem)
        }
    }
}