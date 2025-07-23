package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.X
import ponder.steps.StepProfileRoute
import pondui.ui.behavior.magic
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.*
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond
import pondui.utils.addShadow

@Composable
fun StepProfileScreen(
    route: StepProfileRoute,
) {
    val viewModel = viewModel(key = route.stepId) { StepProfileModel(route) }
    val state by viewModel.stateFlow.collectAsState()
    val appWindow = LocalAppWindow.current
    val nav = LocalNav.current

    val profileStep = state.step ?: return

    profileStep.audioLabelUrl?.let {
        // PlayWave("http://localhost:8080/${it}")
    }

    TitleCloud("Add a step to ${state.step?.label ?: "path"}", state.isAddingStep, viewModel::toggleAddingStep) {
        Column(
            spacingUnits = 1,
            modifier = Modifier.height(400.dp)
                .widthIn(max = 300.dp)
                .fillMaxWidth()
        ) {
            ControlSet(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    text = state.newStepLabel,
                    onTextChanged = viewModel::setNewStepLabel,
                    placeholder = "Enter step name",
                    modifier = Modifier.weight(1f)
                        .takeInitialFocus()
                        .onEnterPressed(viewModel::createStep)
                )
                ControlSetButton("Add", onClick = viewModel::createStep)
            }
            Label("Similar Steps:")
            LazyColumn {
                items(state.similarSteps, key = { it.id }) { step ->
                    StepRow(step, modifier = Modifier.actionable { viewModel.addSimilarStep(step) })
                }
            }
        }
    }

    AddQuestionCloud(
        title = "Add a question",
        stepId = if (state.isAddingQuestion) profileStep.id else null,
        dismiss = viewModel::toggleAddingQuestion
    )

    Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
        TopBarSpacer()

        if (appWindow.widthSizeClass == WindowSizeClass.Compact) {
            Box(
                modifier = Modifier.clip(Pond.ruler.defaultCorners)
                    .magic(offsetX = (-20).dp)
            ) {
                // feature image
                StepImage(
                    url = profileStep.imgUrl,
                    modifier = Modifier.fillMaxWidth()
                        .aspectRatio(1f)
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(.6f))
                ) {
                    // label
                    EditText(
                        text = profileStep.label,
                        placeholder = "Step label",
                        style = Pond.typo.h1.addShadow(),
                        modifier = Modifier.padding(Pond.ruler.unitPadding)
                    ) { viewModel.editStep(profileStep.copy(label = it)) }
                }
            }
            // description
            state.step?.description?.let {
                Text(
                    text = it,
                    style = Pond.typo.bodyLarge,
                    modifier = Modifier.padding(Pond.ruler.unitPadding)
                        .magic(offsetX = 20.dp)
                )
            }
        } else {
            Row(1) {
                // feature image
                StepImage(
                    url = profileStep.imgUrl,
                    modifier = Modifier.weight(1f)
                        .clip(Pond.ruler.defaultCorners)
                        .aspectRatio(1f)
                        .magic(offsetX = (-20).dp)
                )
                Column(
                    spacingUnits = 1,
                    modifier = Modifier.weight(1f)
                        .magic(offsetX = 20.dp)
                ) {
                    // label
                    EditText(
                        text = profileStep.label,
                        placeholder = "Step label",
                        style = Pond.typo.h1.addShadow(),
                        modifier = Modifier.padding(Pond.ruler.unitPadding)
                    ) { viewModel.editStep(profileStep.copy(label = it)) }
                    // description
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

        Tabs("Steps") {
            Tab("Steps") {
                PathEditorView(pathId = route.stepId)
            }
            Tab("Edit", modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                Label("Audio")
                Button("Generate Audio") { viewModel.generateAudio(profileStep) }
                Label("Questions")
                Button("Add Question") { viewModel.toggleAddingQuestion() }
                Label("Tags")
                LazyRow(1) {
                    items(state.tags) { tag ->
                        Row(1) {
                            Text(tag.label)
                            IconButton(TablerIcons.X, Pond.colors.danger) { viewModel.removeTag(tag.id) }
                        }
                    }
                }
                ControlSet {
                    TextField(
                        text = state.newStepLabel,
                        onTextChanged = viewModel::setNewTagLabel,
                        modifier = Modifier.onEnterPressed(viewModel::addNewTag))
                    Button("Add", onClick = viewModel::addNewTag)
                }
            }
            Tab("Activity") {
                StepActivityView(route.stepId)
            }
            Tab("Questions", state.hasQuestions) {
                LazyColumn {
                    items(state.questions, key = { it.id }) { question ->
                        Text(question.text)
                    }
                }
            }
        }
    }
}
