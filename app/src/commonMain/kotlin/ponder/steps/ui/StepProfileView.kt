package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.Plus
import kotlinx.collections.immutable.persistentListOf
import ponder.steps.model.data.Step
import pondui.ui.behavior.Magic
import pondui.ui.behavior.magic
import pondui.ui.behavior.modifyIfTrue
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.selected
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.*
import pondui.ui.theme.Pond
import steps.app.generated.resources.Res

@Composable
fun StepProfileView(
    step: Step,
    navigateStep: (Step) -> Unit
) {
    val viewModel = viewModel { StepProfileModel() }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(step) {
        viewModel.setStep(step)
    }

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

    Column(1) {
        Card(
            innerPadding = 0.dp,
            modifier = Modifier.aspectRatio(3f)
        ) {
            Row {
                StepImage(
                    step = step,
                    modifier = Modifier.weight(1f)
                        .aspectRatio(1f)
                        .magic(offsetX = -20, durationMillis = 500)
                )
                Column(
                    spacingUnits = 1,
                    modifier = Modifier.weight(2f)
                        .padding(Pond.ruler.unitPadding)
                        .magic(offsetX = 20, durationMillis = 500)
                ) {
                    H1(step.label)
                }
            }
        }
        Tabs {
            tab("Steps") {
                LazyColumn(0) {
                    itemsIndexed(state.steps, key = { index, step -> step.id }) { index, step ->
                        val isSelected = state.selectedStepId == step.id
                        Row(
                            spacingUnits = 1,
                            modifier = Modifier.fillMaxWidth()
                                .actionable(isEnabled = !isSelected) { viewModel.selectStep(step.id) }
                                .selected(isSelected)
                                .padding(Pond.ruler.unitPadding)
                                .animateItem()
                        ) {
                            StepItem(
                                step = step,
                                modifier = Modifier.magic(offsetX = index * 10, durationMillis = 500)
                            )
                            Expando()
                            ControlSet(modifier = Modifier.magic(isSelected, scale = true)) {
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
                            Button(
                                TablerIcons.ArrowRight,
                                modifier = Modifier.magic(step.pathSize > 0 || isSelected, rotationZ = -90)
                            ) { navigateStep(step) }
                        }
                    }
                    item("add button") {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().animateItem()) {
                            Button(TablerIcons.Plus, onClick = viewModel::toggleAddingStep)
                        }
                    }
                }
            }
            tab("Activity") {
                Text("No activity")
            }
        }
    }
}