package ponder.steps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Plus
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.Expando
import pondui.ui.controls.H3
import pondui.ui.controls.IconButton
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TextButton
import pondui.ui.theme.Pond

@Composable
fun TrekPathView() {
    val viewModel = viewModel { TrekPathModel() }
    val state by viewModel.state.collectAsState()

    AddStepCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        createIntent = state.trek == null,
        pathId = state.trek?.stepId,
        dismiss = viewModel::toggleAddItem
    )

    LazyColumn(1) {
        item(key = "span") {
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                // MenuWheel(state.span, TrekSpan.entries.toImmutableList()) { viewModel::setSpan }
                Button(TablerIcons.Plus, onClick = viewModel::toggleAddItem)
            }
        }

        state.trek?.let { trekStep ->
            item(trekStep.pathStepId ?: trekStep.trekId ?: trekStep.stepId) {
                Row(1, modifier = Modifier.animateItem()) {
                    H3(trekStep.stepLabel)
                    Expando()
                    IconButton(TablerIcons.ArrowLeft) { viewModel.loadTrek(trekStep.superId) }
                }
            }
        }
        items(state.steps, key = { it.pathStepId ?: it.trekId ?: it.stepId }) { trekStep ->
            Row(1, modifier = Modifier.animateItem()) {
                Text(trekStep.stepLabel)
                Expando()
                val trekId = trekStep.trekId
                if (trekId != null) {
                    IconButton(TablerIcons.ArrowRight) { viewModel.loadTrek(trekId) }
                } else {
                    IconButton(TablerIcons.Plus) { viewModel.branchStep(trekStep.pathStepId) }
                }
            }
        }

        item(key = "bottom spacer") {
            BottomBarSpacer()
        }
    }
}