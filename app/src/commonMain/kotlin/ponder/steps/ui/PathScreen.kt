package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.CircleCheck
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Olympics
import compose.icons.tablericons.Trash
import compose.icons.tablericons.X
import kotlinx.collections.immutable.persistentListOf
import ponder.steps.PathRoute
import pondui.ui.behavior.FadeIn
import pondui.ui.behavior.HotKey
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.*
import pondui.ui.nav.LocalNav
import pondui.ui.nav.Scaffold
import pondui.ui.theme.Pond

// Arr! This be the screen that shows all the root steps - the captains of our plan!
@Composable
fun PathScreen() {
    val viewModel: PathModel = viewModel { PathModel() }
    val state by viewModel.state.collectAsState()
    val nav = LocalNav.current

    LaunchedEffect(state.pathId) {
        nav.setRoute(PathRoute(state.pathId), true)
    }

    HotKey(Key.NumPadAdd, viewModel::toggleAddingStep)

    // Add a cloud dialog for creating new root steps, like a pirate's secret meetin' spot!
    TitleCloud("Add a step", state.isAddingStep, viewModel::toggleAddingStep) {
        ControlSet {
            TextField(
                text = state.newStepLabel,
                onTextChange = viewModel::setNewStepLabel,
                placeholder = "Enter step name",
                modifier = Modifier.takeInitialFocus()
                    .onEnterPressed(viewModel::createNewStep)
            )
            ControlSetButton("Add", onClick = viewModel::createNewStep)
        }
    }

    Scaffold {
        ControlSet(modifier = Modifier.height(Pond.ruler.unitSpacing * 7)) {
            if (state.parent != null) {
                ControlSetButton("Root") { viewModel.refreshItems(null) }
            }
            for (ancestor in state.ancestors) {
                ControlSetButton(ancestor.label) { viewModel.navigateBack(ancestor) }
            }
            state.parent?.let {
                Text("${it.label}:")
            }
        }
        LazyColumn(
            spacingUnits = 1,
            modifier = Modifier.fillMaxWidth()
                .animateContentSize()
        ) {
            items(state.steps, key = { it.id }) { step ->
                Row(1, modifier = Modifier.height(Pond.ruler.unitSpacing * 7)
                    .animateItem()) {

                    FadeIn(rotationZ = 180, scale = true, durationMillis = 500) {
                        AsyncImage(
                            model = "http://localhost:8080/img/horse.png",
                            contentDescription = null,
                            modifier = Modifier.clip(Pond.ruler.round)
                                .fillMaxHeight()
                                .aspectRatio(1f)
                        )
                    }

                    val stepLabelEdit = state.stepLabelEdits.firstOrNull() { it.id == step.id }
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        FadeIn(stepLabelEdit != null, offsetX = 20) {
                            ControlSet {
                                TextField(
                                    text = stepLabelEdit?.label ?: "",
                                    onTextChange = { viewModel.modifyLabelEdit(it, step.id) },
                                    modifier = Modifier.onEnterPressed { viewModel.acceptLabelEdit(stepLabelEdit!!) }
                                        .takeInitialFocus(),
                                    initialSelectAll = true
                                )
                                ControlSetButton(TablerIcons.CircleCheck) { viewModel.acceptLabelEdit(stepLabelEdit!!) }
                            }
                        }
                        FadeIn(stepLabelEdit == null, offsetX = 20) {
                            Text(step.label)
                        }
                    }
                    if (step.children?.isNotEmpty() == true) {
                        FadeIn(offsetX = 20) {
                            Button(TablerIcons.ArrowRight) { viewModel.navigateForward(step) }
                        }
                    }

                    Expando()

                    RowMenu(
                        items = persistentListOf(
                            RowMenuItem(TablerIcons.Trash, Pond.colors.danger) { viewModel.removeStep(step.id) },
                            if (stepLabelEdit != null) {
                                RowMenuItem(TablerIcons.X) { viewModel.cancelLabelEdit(stepLabelEdit) }
                            } else {
                                RowMenuItem(TablerIcons.Edit) { viewModel.startLabelEdit(step) }
                            },
                            RowMenuItem(TablerIcons.Olympics) { viewModel.generateImage(step) },
                            RowMenuItem(TablerIcons.ArrowRight) { viewModel.navigateForward(step)}
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