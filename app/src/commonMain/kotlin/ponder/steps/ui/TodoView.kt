package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import kotlinx.collections.immutable.toImmutableList
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.H2
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.MenuWheel
import pondui.ui.controls.Row
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

    AddIntentCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        dismiss = viewModel::toggleAddItem
    )

    LazyColumn(1, horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            MenuWheel(state.span, TrekSpan.entries.toImmutableList()) { viewModel::setSpan }
        }
        item {
            Row(1, modifier = Modifier.fillMaxWidth()) {
                H2("Upcoming", modifier = Modifier.weight(1f))
                Button("")
            }
        }
        items(state.items, key = { it.trekId }) { item ->
            TrekItemRow(item, viewModel::completeStep)
        }
        item {
            Button(TablerIcons.Plus, onClick = viewModel::toggleAddItem)
        }
        item {
            BottomBarSpacer()
        }
    }
}