package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Drone
import compose.icons.tablericons.Plus
import ponder.steps.model.data.StepId
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.Expando
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TextButton
import pondui.ui.controls.bottomBarSpacerItem
import pondui.ui.controls.topBarSpacerItem
import pondui.ui.theme.Pond

@Composable
fun PathEditorView(
    pathId: StepId,
    viewModel: PathEditorModel = viewModel { PathEditorModel() }
) {
    val editorState by viewModel.state.collectAsState()
    val pathContextState by viewModel.pathContextFlow.collectAsState()

    LaunchedEffect(pathId) {
        viewModel.setParameters(pathId)
    }

    val pathStep = pathContextState.step ?: return

    QuestionEditorCloud(
        questionId = editorState.editQuestionId,
        onDismiss = { viewModel.setEditQuestion(null) },
    )

    LazyColumn(1, Alignment.CenterHorizontally) {
        topBarSpacerItem()

        item("header") {
            PathEditorHeader(
                pathStep = pathStep,
                viewModel = viewModel
            )
        }

        itemsIndexed(pathContextState.steps, key = { index, step -> step.pathStepId ?: step.id }) { index, step ->
            PathEditorItem(
                step = step,
                isSelected = editorState.selectedStepId == step.id,
                isLastStep = (step.position ?: 0) == pathStep.pathSize - 1,
                viewModel = viewModel
            )
        }
        item("add steps") {
            Row(1, modifier = Modifier.padding(Pond.ruler.unitPadding)) {
                Button(TablerIcons.Plus, onClick = viewModel::toggleAddingStep)
                Button(TablerIcons.Drone, onClick = viewModel::suggestNextStep)
            }
        }
        items(editorState.suggestions, key = { it.label }) { suggestion ->
            Column(1) {
                TextButton(
                    suggestion.label,
                    Pond.typo.h3
                ) { viewModel.createStepFromSuggestion(suggestion) }
                suggestion.description?.let { Text(it) }
                Expando(1)
            }
        }

        bottomBarSpacerItem()
    }
}

