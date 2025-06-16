package ponder.steps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import kotlinx.collections.immutable.toImmutableList
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.H3
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.MenuWheel
import pondui.ui.controls.Text

@Composable
fun TrekPathView() {
    val viewModel = viewModel { TrekPathModel() }
    val state by viewModel.state.collectAsState()

    AddIntentCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
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
                H3(trekStep.stepLabel)
            }
        }
        items(state.steps, key = { it.pathStepId ?: it.trekId ?: it.stepId }) { trekStep ->
            Text(trekStep.stepLabel)
        }

        item(key = "bottom spacer") {
            BottomBarSpacer()
        }
    }
}