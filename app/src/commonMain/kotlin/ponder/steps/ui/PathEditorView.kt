package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.Drone
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import ponder.steps.StepProfileRoute
import ponder.steps.model.data.StepId
import pondui.ui.behavior.Magic
import pondui.ui.behavior.magic
import pondui.ui.behavior.selected
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.ControlSet
import pondui.ui.controls.ControlSetButton
import pondui.ui.controls.EditText
import pondui.ui.controls.Expando
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TextButton
import pondui.ui.controls.actionable
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond
import pondui.utils.addShadow

@Composable
fun PathEditorView(
    pathId: StepId,
    viewModel: PathEditorModel = viewModel { PathEditorModel() }
) {
    val state by viewModel.state.collectAsState()
    val nav = LocalNav.current

    LaunchedEffect(pathId) {
        viewModel.setParameters(pathId)
    }

    val pathStep = state.step ?: return

    LazyColumn(1, Alignment.CenterHorizontally) {
        item("header") {
            Column(1, modifier = Modifier.animateContentSize()) {
                Row(1, modifier = Modifier.fillMaxWidth()) {
                    StepImage(
                        url = pathStep.imgUrl,
                        modifier = Modifier.fillMaxWidth(.33f)
                            .heightIn(max = 200.dp)
                            .aspectRatio(1f)
                            .clip(Pond.ruler.defaultCorners)
                    )
                    EditText(
                        text = pathStep.label,
                        placeholder = "Step label",
                        style = Pond.typo.h1.addShadow(),
                        isContainerVisible = true,
                    ) { viewModel.editStep(pathStep.copy(label = it)) }
                }
                Magic(pathStep.description == null) {
                    Row(1) {
                        Button("Add description", onClick = viewModel::addDescription)
                    }
                }
                Magic(pathStep.description != null) {
                    EditText(
                        text = pathStep.description ?: "",
                        placeholder = "Description",
                        isContainerVisible = true,
                        modifier = Modifier.fillMaxWidth()
                    ) { viewModel.editStep(pathStep.copy(description = it)) }
                }
            }
        }

        itemsIndexed(state.steps, key = { index, step -> step.pathStepId ?: step.id }) { index, step ->
            Column(
                spacingUnits = 1,
                modifier = Modifier.animateItem()
            ) {
                val isSelected = state.selectedStepId == step.id
                Row(
                    spacingUnits = 1,
                    modifier = Modifier.fillMaxWidth()
                        .actionable(isEnabled = !isSelected) { viewModel.selectStep(step.id) }
                        .selected(isSelected)
                        .padding(Pond.ruler.unitPadding)
                ) {
                    StepRow(
                        step = step,
                        isEditable = isSelected,
                        modifier = Modifier.weight(1f)
                            .magic(offsetX = index * 10.dp, durationMillis = 500),
                        onImageClick = { nav.go(StepProfileRoute(step.id)) }
                    ) { viewModel.editStep(step.copy(label = it)) }
                    Magic(isSelected, fade = false) {
                        Row(1) {
                            Button(
                                imageVector = TablerIcons.Trash,
                                isEnabled = isSelected,
                                background = Pond.colors.tertiary,
                                modifier = Modifier.magic(isSelected, rotationZ = 360)
                            ) { viewModel.removeStepFromPath(step) }
                            ControlSet(
                                modifier = Modifier.magic(isSelected, scale = true),
                                maxItemsInEachRow = 1
                            ) {
                                ControlSetButton(
                                    imageVector = TablerIcons.ArrowUp,
                                    isEnabled = (step.position ?: 0) > 0,
                                    background = Pond.colors.secondary
                                ) { viewModel.moveStep(step, -1) }
                                ControlSetButton(
                                    imageVector = TablerIcons.ArrowDown,
                                    isEnabled = (step.position ?: 0) < state.steps.size - 1,
                                    background = Pond.colors.secondary
                                ) { viewModel.moveStep(step, 1) }
                            }
                        }
                    }
                    val canStepInto = step.pathSize > 0 || isSelected
                    Button(
                        imageVector = TablerIcons.ArrowRight,
                        isEnabled = canStepInto,
                        modifier = Modifier.magic(canStepInto, offsetX = (-32).dp)
                    ) { nav.go(StepProfileRoute(step.id)) }
                }
            }
        }
        item("add steps") {
            Row(1, modifier = Modifier.padding(Pond.ruler.unitPadding)) {
                Button(TablerIcons.Plus, onClick = viewModel::toggleAddingStep)
                Button(TablerIcons.Drone, onClick = viewModel::suggestNextStep)
            }
        }
        items(state.suggestions, key = { it.label }) { suggestion ->
            Column(1) {
                TextButton(
                    suggestion.label,
                    Pond.typo.h3
                ) { viewModel.createStepFromSuggestion(suggestion) }
                suggestion.description?.let { Text(it) }
                Expando(1)
            }
        }
    }
}