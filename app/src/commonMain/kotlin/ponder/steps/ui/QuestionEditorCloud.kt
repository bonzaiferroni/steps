package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPlay
import org.jetbrains.compose.ui.tooling.preview.Preview
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question
import ponder.steps.model.data.QuestionId
import pondui.LocalWavePlayer
import pondui.ui.behavior.MagicItem
import pondui.ui.controls.Button
import pondui.ui.controls.Checkbox
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.EditText
import pondui.ui.controls.Expando
import pondui.ui.controls.Label
import pondui.ui.controls.LabeledCheckbox
import pondui.ui.controls.Row
import pondui.ui.controls.TextField
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
    val dispatch = viewModel::dispatch

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onDismiss()
    }

    Column(1) {
        TextField(
            label = "Question text",
            text = question.text,
            placeholder = "Question text",
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        ) { dispatch(EditQuestion(question.copy(text = it))) }
        Row(1) {
            Expando()
            DropMenu(question.type) { dispatch(EditQuestion(question.copy(type = it))) }
        }
        if (question.type == DataType.Integer || question.type == DataType.Decimal) {
            Row(1) {
                TextField(
                    label = "min value",
                    text = state.minValue ?: "",
                    onTextChanged = { dispatch(EditQuestionMinValue(it))},
                    placeholder = "optional",
                    modifier = Modifier.weight(1f)
                )

                TextField(
                    label = "max value",
                    text = state.maxValue ?: "",
                    onTextChanged = { dispatch(EditQuestionMaxValue(it))},
                    placeholder = "optional",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(1) {
            LabeledCheckbox("Generate speech", state.generateAudio) { dispatch(ToggleQuestionAudio) }
            val audioUrl = question.audioUrl
            Button(TablerIcons.PlayerPlay, isEnabled = audioUrl != null) { audioUrl?.let { wavePlayer.play(it) } }
            // other toggle options
        }
        Row(1) {
            Expando()
            Button("Delete", Pond.colors.danger) { dispatch(DeleteQuestion) }
            Button("Cancel", Pond.colors.tertiary, onClick = onDismiss)
            Button("Accept") { dispatch(AcceptQuestionEdit) }
        }
    }
}