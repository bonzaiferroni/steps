package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.PlayerPlay
import ponder.steps.model.data.DataType
import pondui.LocalWavePlayer
import pondui.ui.behavior.MagicItem
import pondui.ui.behavior.selected
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.DropMenu
import pondui.ui.controls.Expando
import pondui.ui.controls.LabeledCheckbox
import pondui.ui.controls.Row
import pondui.ui.controls.TextField
import pondui.ui.controls.TitleCloud
import pondui.ui.theme.Pond

@Composable
fun QuestionEditorCloud(
    request: EditQuestionRequest?,
    onDismiss: () -> Unit,
) {
    TitleCloud(
        title = "Edit Question",
        isVisible = request != null,
        onDismiss = onDismiss
    ) {
        MagicItem(request) {
            if (request != null) {
                QuestionEditorView(
                    request = request,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
fun QuestionEditorView(
    request: EditQuestionRequest,
    onDismiss: () -> Unit,
    viewModel: QuestionEditorModel = viewModel { QuestionEditorModel() }
) {
    val state by viewModel.stateFlow.collectAsState()
    val wavePlayer = LocalWavePlayer.current

    MessengerView(viewModel.messenger)

    LaunchedEffect(request) {
        viewModel.setParameters(request)
    }

    val question = state.question ?: return
    val dispatch = viewModel::dispatch

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onDismiss()
    }

    Column(2) {
        TextField(
            text = question.text,
            placeholder = "Question text",
            modifier = Modifier.fillMaxWidth()
                .selected(state.isTextError, Pond.colors.danger),
            label = "question text",
            minLines = 2
        ) { dispatch(EditQuestion(question.copy(text = it))) }
        Row(1) {
            DropMenu(
                selected = question.type,
                label = "question type",
                color = Pond.colors.secondary
            ) { dispatch(EditQuestion(question.copy(type = it))) }
        }
        if (question.type == DataType.Integer || question.type == DataType.Decimal) {
            Row(1) {
                TextField(
                    text = state.minValue ?: "",
                    label = "min value",
                    onTextChanged = { dispatch(EditQuestionMinValue(it)) },
                    placeholder = "optional",
                    modifier = Modifier.weight(1f)
                )

                TextField(
                    text = state.maxValue ?: "",
                    label = "max value",
                    onTextChanged = { dispatch(EditQuestionMaxValue(it)) },
                    placeholder = "optional",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(1) {
            LabeledCheckbox("Generate speech", state.generateAudio) { dispatch(ToggleQuestionAudio) }
            val audioUrl = question.audioUrl
            Button(
                imageVector = TablerIcons.PlayerPlay,
                background = Pond.colors.secondary,
                isEnabled = audioUrl != null
            ) { audioUrl?.let { wavePlayer.play(it) } }
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