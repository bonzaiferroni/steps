package ponder.steps.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPlay
import org.jetbrains.compose.ui.tooling.preview.Preview
import ponder.steps.model.data.Question
import ponder.steps.model.data.QuestionId
import pondui.LocalWavePlayer
import pondui.ui.behavior.MagicItem
import pondui.ui.controls.Button
import pondui.ui.controls.Checkbox
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.EditText
import pondui.ui.controls.Row
import pondui.ui.controls.TitleCloud
import pondui.ui.theme.Pond

@Composable
fun QuestionEditorCloud(
    questionId: QuestionId?,
    onDismiss: () -> Unit,
) {
    TitleCloud(
        title = "Edit Question",
        isVisible = questionId != null,
        onDismiss = onDismiss
    ) {
        MagicItem(questionId) {
            if (questionId != null) {
                QuestionEditorView(questionId, onDismiss)
            }
        }
    }
}

@Composable
fun QuestionEditorView(
    questionId: QuestionId,
    onDismiss: () -> Unit,
    viewModel: QuestionEditorModel = viewModel (key = questionId) { QuestionEditorModel(questionId) }
) {
    val state by viewModel.state.collectAsState()
    val wavePlayer = LocalWavePlayer.current
    val question = state.question ?: return

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onDismiss()
    }

    Column(1) {
        EditText(
            text = question.text,
            placeholder = "Question text",
            isContainerVisible = true,
        ) { viewModel.editQuestion(question.copy(text = it)) }
        DropMenu(question.type) { viewModel.editQuestion(question.copy(type = it)) }
        val audioUrl = question.audioUrl
        if (audioUrl != null) {
            Button(TablerIcons.PlayerPlay) { wavePlayer.play(audioUrl) }
        }
        Row(1) {
            // Checkbox()
        }
        Row(1) {
            Button("Delete", Pond.colors.danger, onClick = viewModel::deleteQuestion)
            Button("Cancel", Pond.colors.tertiary, onClick = onDismiss)
            Button("Accept", onClick = viewModel::acceptEdit)
        }
    }
}