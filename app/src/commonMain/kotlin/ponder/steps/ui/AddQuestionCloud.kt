package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import ponder.steps.db.QuestionDao
import ponder.steps.db.QuestionEntity
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question
import pondui.ui.behavior.onEnterPressed
import pondui.ui.behavior.takeInitialFocus
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.ControlSet
import pondui.ui.controls.ControlSetButton
import pondui.ui.controls.Label
import pondui.ui.controls.MenuWheel
import pondui.ui.controls.TextField
import pondui.ui.controls.TitleCloud
import pondui.ui.core.StateModel
import java.util.UUID

@Composable
fun AddQuestionCloud(title: String, stepId: String?, dismiss: () -> Unit) {
    val viewModel = viewModel { AddQuestionModel(dismiss) }
    val state by viewModel.state.collectAsState()

    // Set the stepId in the model
    viewModel.setStepId(stepId)

    TitleCloud(
        title = title,
        isVisible = stepId != null,
        onDismiss = dismiss
    ) {
        Column(
            spacingUnits = 1,
            modifier = Modifier.height(400.dp),
        ) {
            // Question text input
            Label("Question Text:")
            TextField(
                text = state.questionText,
                onTextChange = viewModel::setQuestionText,
                placeholder = "Enter question text",
                modifier = Modifier.fillMaxWidth()
                    .takeInitialFocus()
                    .onEnterPressed(viewModel::createQuestion)
            )

            // Question type selection
            Label("Question Type:")
            MenuWheel(
                selectedItem = state.questionType,
                options = DataType.entries.toImmutableList(),
                onSelect = viewModel::setQuestionType,
            )

            // Min and Max values for numeric types
            if (state.questionType == DataType.Integer || state.questionType == DataType.Float) {
                Label("Min Value (optional):")
                TextField(
                    text = state.minValue ?: "",
                    onTextChange = viewModel::setMinValue,
                    placeholder = "Enter minimum value",
                    modifier = Modifier.fillMaxWidth()
                )

                Label("Max Value (optional):")
                TextField(
                    text = state.maxValue ?: "",
                    onTextChange = viewModel::setMaxValue,
                    placeholder = "Enter maximum value",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Create button
            Button(
                text = "Create",
                isEnabled = state.isValidQuestion,
                onClick = viewModel::createQuestion
            )
        }
    }
}

