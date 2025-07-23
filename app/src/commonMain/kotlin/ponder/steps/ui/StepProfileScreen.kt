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
    val pathContextState by viewModel.pathContext.stateFlow.collectAsState()
    val nav = LocalNav.current

    val profileStep = pathContextState.step ?: return

    profileStep.audioLabelUrl?.let {
        // PlayWave("http://localhost:8080/${it}")
    }

    Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
        TopBarSpacer()

        Tabs("Steps") {
            Tab("Steps") {
                PathContextView(viewModel.pathContext)
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
                    items(pathContextState.tags) { tag ->
                        Row(1) {
                            Text(tag.label)
                            IconButton(TablerIcons.X, Pond.colors.danger) { viewModel.removeTag(tag.id) }
                        }
                    }
                }
                ControlSet {
                    TextField(
                        text = state.newTagLabel,
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
