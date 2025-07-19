package ponder.steps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ponder.steps.model.data.Question
import pondui.ui.controls.DropMenu
import pondui.ui.controls.EditText
import pondui.ui.controls.TitleCloud

@Composable
fun QuestionEditorCloud(
    question: Question?,
    onDismiss: () -> Unit,
) {
    TitleCloud(
        title = "Edit Question",
        isVisible = question != null,
        onDismiss = onDismiss
    ) {
        if (question == null) return@TitleCloud

        var question by remember(question.id) { mutableStateOf(question) }

        EditText(
            text = question.text,
            placeholder = "Question text",
            isContainerVisible = true,
        ) { question = question.copy(text = it) }
        DropMenu(question.type) { question = question.copy(type = it) }
    }
}