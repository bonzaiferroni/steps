package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sun.rowset.internal.Row
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import ponder.steps.StepProfileRoute
import pondui.ui.behavior.ifTrue
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.Button
import pondui.ui.controls.Checkbox
import pondui.ui.controls.Column
import pondui.ui.controls.ControlSet
import pondui.ui.controls.ControlSetButton
import pondui.ui.controls.FlowRow
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.TitleCloud
import pondui.ui.controls.actionable
import pondui.ui.nav.LocalNav

@Composable
fun TodoView() {
    val viewModel = viewModel { TodoModel() }
    val state by viewModel.state.collectAsState()
    val nav = LocalNav.current

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
            ControlSet(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    text = state.newStepLabel,
                    onTextChange = viewModel::setNewStepLabel,
                    modifier = Modifier.weight(1f)
                        .takeInitialFocus()
                        .onEnterPressed(viewModel::createStep)
                )
                ControlSetButton("Create", onClick = viewModel::createStep)
            }
            LazyColumn(1) {
                items(state.searchedSteps) { step ->
                    StepItem(step, modifier = Modifier.actionable { viewModel.addSearchedStep(step) })
                }
            }
        }
    }

    LazyColumn(1, horizontalAlignment = Alignment.CenterHorizontally) {
        items(state.items, key = { it.trekId }) { item ->
            val isFinished = item.finishedAt != null
            FlowRow(
                unitSpacing = 1,
                modifier = Modifier.fillMaxWidth()
                    .animateItem()
                    .ifTrue(isFinished) { alpha(.5f) }
            ) {
                Checkbox(item.finishedAt != null) { viewModel.completeStep(item) }
                StepItem(
                    label = item.stepLabel,
                    thumbUrl = item.stepThumbUrl,
                    description = item.stepDescription,
                    onImageClick = { nav.go(StepProfileRoute(item.stepId))},
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Button(TablerIcons.Plus, onClick = viewModel::toggleAddItem)
        }
    }
}