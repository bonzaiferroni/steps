package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.Drone
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import ponder.steps.model.data.Step
import pondui.ui.behavior.Magic
import pondui.ui.behavior.magic
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.selected
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.*
import pondui.ui.theme.Pond
import pondui.utils.addShadow

@Composable
fun StepProfileView(
    step: Step,
    navigateStep: (Step) -> Unit
) {
    val viewModel = viewModel { StepProfileModel() }
    val state by viewModel.state.collectAsState()
    val appWindow = LocalAppWindow.current
    LaunchedEffect(appWindow) {
        println(appWindow.width)
    }

    LaunchedEffect(step) {
        viewModel.setStep(step)
    }

    val profileStep = state.step ?: return

    TitleCloud("Add a step to ${state.step?.label ?: "path"}", state.isAddingStep, viewModel::toggleAddingStep) {
        Column(1, modifier = Modifier.height(400.dp)) {
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
            Label("Similar Steps:")
            LazyColumn {
                items(state.similarSteps, key = { it.id }) { step ->
                    StepItem(step, modifier = Modifier.actionable { viewModel.addSimilarStep(step) })
                }
            }
        }
    }

    val cloudStep = state.cloudStep
    var cloudStepLabel by remember { mutableStateOf("") }
    val currentCloudStepLabel = cloudStep?.label
    LaunchedEffect(currentCloudStepLabel) {
        currentCloudStepLabel?.let { cloudStepLabel = it }
    }

    TitleCloud(
        title = cloudStepLabel,
        isVisible = cloudStep != null,
        onDismiss = { viewModel.setCloudStep(null) }
    ) {
        Column(1) {
            StepImage(
                url = cloudStep?.imgUrl,
                modifier = Modifier.clip(Pond.ruler.defaultCorners)
            )
            Button("Generate") { viewModel.generateImage(cloudStep!!) }
        }
    }

    Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
        if (appWindow.widthSizeClass == WindowSizeClass.Compact) {
            Box(modifier = Modifier.clip(Pond.ruler.defaultCorners)) {
                StepImage(
                    url = step.imgUrl,
                    modifier = Modifier.fillMaxWidth()
                        .aspectRatio(1f)
                    // .magic(offsetX = -20, durationMillis = 500)
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(.6f))
                    // .magic(offsetY = 50, durationMillis = 500)
                ) {
                    EditText(
                        text = step.label,
                        placeholder = "Step label",
                        style = Pond.typo.h1,
                        modifier = Modifier.padding(Pond.ruler.unitPadding)
                    ) { viewModel.editStep(step.copy(label = it)) }
                }
            }
            state.step?.description?.let {
                Text(
                    text = it,
                    style = Pond.typo.bodyLarge,
                    modifier = Modifier.padding(Pond.ruler.unitPadding)
                )
            }
        } else {
            Row(1) {
                StepImage(
                    url = step.imgUrl,
                    modifier = Modifier.weight(1f)
                        .clip(Pond.ruler.defaultCorners)
                        .aspectRatio(1f)
                    // .magic(offsetX = -20, durationMillis = 500)
                )
                Column(1, modifier = Modifier.weight(1f)) {
                    EditText(
                        text = step.label,
                        placeholder = "Step label",
                        style = Pond.typo.h1,
                        modifier = Modifier.padding(Pond.ruler.unitPadding)
                    ) { viewModel.editStep(step.copy(label = it)) }
                    state.step?.description?.let {
                        Text(
                            it,
                            Pond.typo.bodyLarge,
                            modifier = Modifier.padding(Pond.ruler.unitPadding)
                        )
                    }
                }
            }
        }

        Tabs {
            Tab("Steps") {
                LazyColumn(0, Alignment.CenterHorizontally) {
                    itemsIndexed(state.steps, key = { index, step -> step.id }) { index, step ->
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
                                StepItem(
                                    step = step,
                                    isEditable = isSelected,
                                    modifier = Modifier.weight(1f)
                                        .magic(offsetX = index * 10, durationMillis = 500),
                                    onImageClick = { viewModel.setCloudStep(step) }
                                ) { viewModel.editStep(step.copy(label = it)) }
                                Magic(isSelected, fade = false) {
                                    Row(1) {
                                        Button(
                                            imageVector = TablerIcons.Trash,
                                            isEnabled = isSelected,
                                            background = Pond.colors.tertiary,
                                            modifier = Modifier.magic(isSelected, rotationZ = 360)
                                        ) { viewModel.remoteStepFromPath(step) }
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
                                    modifier = Modifier.magic(canStepInto, offsetX = -32)
                                ) { navigateStep(step) }
                            }
                        }
                    }
                    item("add steps") {
                        Row(1, modifier = Modifier.padding(Pond.ruler.unitPadding).animateItem()) {
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
            Tab("Edit") {
                Label("Description")
                EditText(
                    text = profileStep.description ?: "",
                    placeholder = "Step Description",
                    modifier = Modifier.padding(horizontal = 32.dp),
                ) { viewModel.editStep(profileStep.copy(description = it)) }
                Label("Theme")
                EditText(
                    text = profileStep.theme ?: "",
                    placeholder = "Image Theme",
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) { viewModel.editStep(profileStep.copy(theme = it)) }
                Label("Image")
                Button("Generate") { viewModel.generateImage(profileStep) }
            }
            Tab("Activity") {
                Text("No activity")
            }
        }
    }
}