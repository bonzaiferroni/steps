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
import ponder.steps.PathsRoute
import pondui.ui.behavior.HotKey
import pondui.ui.behavior.Magic
import pondui.ui.behavior.magic
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.*
import pondui.ui.theme.Pond

@Composable
fun PathsScreen(
    route: PathsRoute
) {
    val viewModel: PathsModel = viewModel { PathsModel(route.pathId) }
    val state by viewModel.state.collectAsState()

    HotKey(Key.NumPadAdd, viewModel::toggleAddingStep)

    TitleCloud("Add a step", state.isAddingStep, viewModel::toggleAddingStep) {
        ControlSet {
            TextField(
                text = state.newStepLabel,
                onTextChange = viewModel::setNewStepLabel,
                placeholder = "Enter step name",
                modifier = Modifier.takeInitialFocus()
                    .onEnterPressed(viewModel::createStep)
            )
            ControlSetButton("Add", onClick = viewModel::createStep)
        }
    }

    Scaffold {
        Column(1) {
            Row(1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(-Pond.ruler.unitSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.step != null) {
                        Button(TablerIcons.ArrowUp) { viewModel.navigateTop() }
                    }
                    for (step in state.breadCrumbs) {
                        StepImage(
                            step.thumbUrl,
                            modifier = Modifier.height(40.dp)
                                .clip(CircleShape)
                                .actionable { viewModel.navigateCrumb(step) }
                        )
                    }
                }
                Expando()
                TextField(state.searchText, viewModel::setSearchText)
                Button(TablerIcons.Plus, onClick = viewModel::toggleAddingStep)
            }
            Box {
                val isProfileVisible = state.showProfile && state.step != null
                Magic(!isProfileVisible) {
                    LazyColumn(0) {
                        itemsIndexed(state.steps, key = { index, step -> step.id }) { index, step ->
                            StepItem(
                                step = step,
                                modifier = Modifier.actionable { viewModel.navigateStep(step) }
                                    .fillMaxWidth()
                                    .padding(Pond.ruler.unitPadding)
                                    .animateItem()
                                    .magic(offsetX = index * 10, durationMillis = 500),
                            )
                        }
                    }
                }
                val step = state.step ?: return@Box
                Magic(isProfileVisible) {
                    StepProfileView(step, viewModel::navigateStep)
                }
            }
        }
    }
}
