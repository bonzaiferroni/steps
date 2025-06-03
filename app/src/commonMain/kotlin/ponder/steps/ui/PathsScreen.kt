package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.Plus
import ponder.steps.PathsRoute
import pondui.ui.behavior.HotKey
import pondui.ui.behavior.Magic
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.*

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
                    .onEnterPressed(viewModel::createRootStep)
            )
            ControlSetButton("Add", onClick = viewModel::createRootStep)
        }
    }

    Scaffold {
        Column(1) {
            Row(1) {
                Row(

                ) {
                    if (state.step != null) {
                        Button(TablerIcons.ArrowUp) { viewModel.navigateCrumb(null) }
                    }
                }
                Expando()
                TextField(state.searchText, viewModel::setSearchText)
                Button(TablerIcons.Plus, onClick = viewModel::toggleAddingStep)
            }
            Box {
                val isProfileVisible = !state.isSearching && state.step != null
                Magic(!isProfileVisible) {
                    LazyColumn(1) {
                        items(state.steps) { step ->
                            TextButton(step.label) { viewModel.setStep(step) }
                        }
                    }
                }
                val step = state.step ?: return@Box
                Magic(isProfileVisible) {
                    StepProfileView(step)
                }
            }
        }
    }
}
