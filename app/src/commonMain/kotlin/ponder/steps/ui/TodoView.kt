package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import compose.icons.tablericons.X
import kotlinx.collections.immutable.toImmutableList
import ponder.steps.StepProfileRoute
import ponder.steps.model.data.IntentPriority
import pondui.ui.behavior.Magic
import pondui.ui.behavior.ifTrue
import pondui.ui.behavior.magic
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.Button
import pondui.ui.controls.Checkbox
import pondui.ui.controls.Column
import pondui.ui.controls.ControlSet
import pondui.ui.controls.ControlSetButton
import pondui.ui.controls.DateTimeWheel
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Label
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.MenuWheel
import pondui.ui.controls.Row
import pondui.ui.controls.Tab
import pondui.ui.controls.Tabs
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.TimeWheel
import pondui.ui.controls.TitleCloud
import pondui.ui.controls.actionable
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond
import pondui.utils.rememberLastNonNull

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

    TitleCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        onDismiss = viewModel::toggleAddItem
    ) {
        Column(
            spacingUnits = 1,
            modifier = Modifier.height(400.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                ControlSet(
                    modifier = Modifier.fillMaxWidth()
                         .magic(state.intentStep == null, rotationX = 90)
                ) {
                    TextField(
                        text = state.intentLabel,
                        onTextChange = viewModel::setNewStepLabel,
                        modifier = Modifier.weight(1f)
                            .takeInitialFocus()
                            .onEnterPressed(viewModel::createStep)
                    )
                    ControlSetButton("Create", onClick = viewModel::createStep)
                }
                Row(
                    spacingUnits = 1,
                    modifier = Modifier.fillMaxWidth()
                        .magic(state.intentStep != null, rotationX = 90)
                ) {
                    val step = rememberLastNonNull(state.intentStep) ?: return@Row
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
                    ControlSet {
                        ControlSetButton(TablerIcons.X, Pond.colors.tertiary) { viewModel.setIntentStep(null) }
                        ControlSetButton("Add", onClick = viewModel::createStep)
                    }
                }
            }
            FlowRow(
                1,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) {
                Row(1, modifier = Modifier.padding(horizontal = Pond.ruler.unitSpacing)) {
                    Label("Priority:")
                    Text(state.intentPriority.name)
                }
                Row(1, modifier = Modifier.padding(horizontal = Pond.ruler.unitSpacing)) {
                    Label("Happens:")
                    Text(state.scheduleDescription)
                }
            }

            Tabs {
                Tab("Existing Steps") {
                    LazyColumn(1) {
                        items(state.searchedSteps) { step ->
                            StepItem(step, modifier = Modifier.actionable { viewModel.setIntentStep(step) })
                        }
                    }
                }
                Tab("Adjust") {
                    Column(1, modifier = Modifier.fillMaxWidth()) {
                        Label("Schedule")
                        Row(1) {
                            MenuWheel(
                                selectedItem = state.intentTiming,
                                options = IntentTiming.entries.toImmutableList(),
                                onSelect = viewModel::setIntentTiming,
                            )
                            Box {
                                Magic(state.intentTiming == IntentTiming.Repeat, offsetX = 40) {
                                    Row(1) {
                                        Label("every")
                                        MenuWheel(
                                            selectedItem = state.intentRepeatValue,
                                            options = state.repeatValues,
                                            onSelect = viewModel::setIntentRepeat,
                                        )
                                        MenuWheel(
                                            selectedItem = state.intentRepeatUnit,
                                            options = TimeUnit.entries.toImmutableList(),
                                            onSelect = viewModel::setIntentRepeatUnit,
                                            itemAlignment = Alignment.Start
                                        )
                                        val canSChedule = state.intentRepeatUnit > TimeUnit.Hours
                                        Row(
                                            spacingUnits = 1,
                                            modifier = Modifier.magic(canSChedule, offsetX = 40)
                                        ) {
                                            Label("at")
                                            TimeWheel(
                                                instant = state.intentScheduledAt,
                                                onChangeInstant = viewModel::setScheduleAt,
                                            )
                                        }
                                    }
                                }
                                Magic(state.intentTiming == IntentTiming.Schedule, offsetX = 40) {
                                    Row(1) {
                                        Label("at")
                                        DateTimeWheel(
                                            state.intentScheduledAt,
                                            onChangeInstant = viewModel::setScheduleAt
                                        )
                                    }
                                }
                            }
                        }
                        Label("Priority")
                        MenuWheel(
                            selectedItem = state.intentPriority,
                            options = IntentPriority.entries.toImmutableList(),
                            onSelect = viewModel::setIntentPriority,
                            itemAlignment = Alignment.CenterHorizontally,
                        )
                    }
                }
            }
        }
    }

    LazyColumn(1, horizontalAlignment = Alignment.CenterHorizontally) {
        items(state.items, key = { it.trekId }) { item ->
            val isFinished = item.finishedAt != null
            FlowRow(
                unitSpacing = 1,
                modifier = Modifier.fillMaxWidth()
                    .animateItem()
                    .ifTrue(isFinished) { alpha(.5f) }
            ) {
                Checkbox(item.finishedAt != null) { viewModel.completeStep(item) }
                StepItem(
                    label = item.stepLabel,
                    thumbUrl = item.stepThumbUrl,
                    description = item.stepDescription,
                    onImageClick = { nav.go(StepProfileRoute(item.stepId)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Button(TablerIcons.Plus, onClick = viewModel::toggleAddItem)
        }
    }
}