package ponder.steps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.Plus
import ponder.steps.StepProfileRoute
import pondui.ui.behavior.HotKey
import pondui.ui.behavior.Magic
import pondui.ui.behavior.magic
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.*
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond

@Composable
fun PathsScreen() {
    val viewModel: PathsModel = viewModel { PathsModel() }
    val state by viewModel.state.collectAsState()
    val nav = LocalNav.current

    HotKey(Key.NumPadAdd, viewModel::toggleAddingStep)

    TitleCloud("Add a step", state.isAddingStep, viewModel::toggleAddingStep) {
        ControlSet {
            TextField(
                text = state.newStepLabel,
                onTextChange = viewModel::setNewStepLabel,
                placeholder = "Enter step name",
                modifier = Modifier.takeInitialFocus()
                    .onEnterPressed { viewModel.createStep { nav.go(StepProfileRoute(it)) }}
            )
            ControlSetButton("Add") { viewModel.createStep { nav.go(StepProfileRoute(it)) }}
        }
    }

    Scaffold {
        Column(1) {
            Row(1) {
                Expando()
                TextField(state.searchText, viewModel::setSearchText)
                Button(TablerIcons.Plus, onClick = viewModel::toggleAddingStep)
            }
            LazyColumn(0) {
                itemsIndexed(state.steps, key = { index, step -> step.id }) { index, step ->
                    StepItem(
                        step = step,
                        modifier = Modifier.actionable { nav.go(StepProfileRoute(step.id)) }
                            .fillMaxWidth()
                            .padding(Pond.ruler.unitPadding)
                            .animateItem()
                            .magic(offsetX = index * 10, durationMillis = 500),
                    )
                }
            }
        }
    }
}
