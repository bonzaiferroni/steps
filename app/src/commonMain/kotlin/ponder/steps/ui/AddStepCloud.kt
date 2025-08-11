package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.X
import kabinet.utils.pluralize
import ponder.steps.model.data.StepId
import pondui.ui.behavior.MagicItem
import pondui.ui.behavior.magic
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.Column
import pondui.ui.controls.ControlSet
import pondui.ui.controls.ControlSetButton
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Label
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.controls.Tab
import pondui.ui.controls.Tabs
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.TitleCloud
import pondui.ui.controls.actionable
import pondui.ui.theme.Pond

@Composable
fun AddStepCloud(
    title: String,
    isVisible: Boolean,
    createIntent: Boolean,
    pathId: StepId?,
    dismiss: () -> Unit
) {
    val viewModel = viewModel { AddStepModel(dismiss) }
    val state by viewModel.stateFlow.collectAsState()
    val adjustIntentState by viewModel.editIntent.stateFlow.collectAsState()

    LaunchedEffect(createIntent, pathId) {
        viewModel.setParameters(createIntent, pathId)
    }

    TitleCloud(
        title = title,
        isVisible = isVisible,
        onDismiss = dismiss
    ) {
        Column(
            gap = 1,
            modifier = Modifier.height(400.dp),
        ) {
            MagicItem(
                item = state.existingStep,
                rotationX = 90,
                modifier = Modifier.height(44.dp),
                itemContent = { step ->
                    Row(
                        gap = 1,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StepImage(
                            url = step.imgUrl,
                            modifier = Modifier.height(40.dp)
                                .clip(CircleShape)
                        )
                        Text(
                            text = step.label,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                        if (step.pathSize > 0) {
                            Label("${step.pathSize} step${pluralize(step.pathSize)}")
                        }
                        ControlSet {
                            ControlSetButton(TablerIcons.X, Pond.colors.regression) { viewModel.setIntentStep(null) }
                            ControlSetButton("Add", onClick = viewModel::addExistingStep)
                        }
                    }
                }
            ) {
                ControlSet(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        text = state.intentLabel,
                        onTextChanged = viewModel::setNewStepLabel,
                        modifier = Modifier.weight(1f)
                            .takeInitialFocus()
                            .onEnterPressed(viewModel::createStep)
                    )
                    ControlSetButton("Create", onClick = viewModel::createStep)
                }
            }
            if (state.createIntent) {
                FlowRow(
                    1,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth().animateContentSize()
                ) {
                    Row(1, modifier = Modifier.padding(horizontal = Pond.ruler.unitSpacing)) {
                        Label("Priority:")
                        Text(adjustIntentState.priority.name)
                    }
                    Row(1, modifier = Modifier.padding(horizontal = Pond.ruler.unitSpacing)) {
                        Label("Happens:")
                        Text(adjustIntentState.scheduleDescription)
                    }
                }
            }

            Tabs {
                Tab("Existing Steps") {
                    LazyColumn(1) {
                        itemsIndexed(state.searchedSteps, key = { index, it -> it.id }) { index, step ->
                            StepRow(
                                step, modifier = Modifier.actionable { viewModel.setIntentStep(step) }
                                    .animateItem()
                                    .magic(offsetX = 20.dp * index)
                            )
                        }
                    }
                }
                Tab("Adjust", state.createIntent) {
                    EditIntentView(viewModel.editIntent)
                }
            }
        }
    }
}

enum class IntentTiming {
    Repeat,
    Once,
    Schedule,
}