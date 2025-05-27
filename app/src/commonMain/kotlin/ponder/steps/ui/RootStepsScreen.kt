package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Trash
import kotlinx.collections.immutable.persistentListOf
import pondui.ui.behavior.HotKey
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.*
import pondui.ui.nav.Scaffold
import pondui.ui.theme.Pond
import pondui.ui.theme.Spacing

// Arr! This be the screen that shows all the root steps - the captains of our plan!
@Composable
fun RootStepsScreen() {
    val viewModel: RootStepsModel = viewModel { RootStepsModel() }
    val state by viewModel.state.collectAsState()

    HotKey(Key.NumPadAdd, viewModel::toggleAddingStep)

    // Add a cloud dialog for creating new root steps, like a pirate's secret meetin' spot!
    TitleCloud("Add a step", state.isAddingStep, viewModel::toggleAddingStep) {
        ControlSet {
            TextField(
                text = state.newStepLabel,
                onTextChange = viewModel::setNewStepLabel,
                placeholder = "Enter step name",
                modifier = Modifier.takeInitialFocus()
                    .onEnterPressed(viewModel::createNewRootStep)
            )
            ControlSetButton("Add", onClick = viewModel::createNewRootStep)
        }
    }

    Scaffold {
        LazyColumn(
            spacingUnits = 1,
            modifier = Modifier.fillMaxWidth()
                .animateContentSize()
        ) {
            items(state.rootSteps, key = { it.id }) { step ->
                Row(1, modifier = Modifier.animateItem()) {
                    Text(step.label)
                    Expando()
                    RowMenu(items = persistentListOf(
                        RowMenuItem(TablerIcons.Trash, Pond.colors.danger) { viewModel.removeStep(step.id) }
                    ))
                }
            }
        }

        // Add a button to create new root steps, like a call to arms for new recruits!
        Button(
            text = "Add",
            onClick = viewModel::toggleAddingStep,
        )
    }
}