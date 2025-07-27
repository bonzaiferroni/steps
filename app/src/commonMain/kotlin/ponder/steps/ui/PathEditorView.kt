package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.Drone
import compose.icons.tablericons.Plus
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import pondui.ui.behavior.AlignX
import pondui.ui.behavior.drawLabel
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.EditText
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
    val state by viewModel.stateFlow.collectAsState()
    val pathContextState by viewModel.pathContext.stateFlow.collectAsState()

    LaunchedEffect(pathId) {
        viewModel.setParameters(pathId)
    }

    val pathStep = pathContextState.step ?: return

    QuestionEditorCloud(
        request = state.editQuestionRequest,
        onDismiss = { viewModel.setEditQuestionRequest(null) },
    )

    fun getStep(index: Int): Step? {
        val steps = pathContextState.steps
        val newStepPosition = state.newStepPosition
        if (newStepPosition == null) {
            if (index == steps.size) return null
            return steps[index]
        }

        if (index == newStepPosition) return null
        val index = if (index > newStepPosition) index - 1 else index
        return pathContextState.steps[index]
    }

    LazyColumn(1, Alignment.CenterHorizontally) {
        topBarSpacerItem()

        item("header") {
            PathEditorHeader(
                pathStep = pathStep,
                viewModel = viewModel
            )
        }

        items((0..pathContextState.steps.size).toList(), key = { index ->
            getStep(index)?.let { it.pathStepId ?: it.id } ?: "new-step"
        }) { index ->
            val step = getStep(index)
            if (step != null) {
                PathEditorStep(
                    step = step,
                    isSelected = pathContextState.selectedStepId == step.id,
                    isLastStep = (step.position ?: 0) == pathStep.pathSize - 1,
                    viewModel = viewModel
                )
            } else {
                NewPathEditorStep(
                    newStepLabel = state.newStepLabel,
                    isLastPosition = state.newStepPosition == null,
                    viewModel = viewModel
                )
            }

        }
        item("add steps") {
            Row(1, modifier = Modifier.padding(Pond.ruler.unitPadding)) {
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

        bottomBarSpacerItem()
    }
}

@Composable
fun LazyItemScope.NewPathEditorStep(
    newStepLabel: String,
    isLastPosition: Boolean,
    viewModel: PathEditorModel,
) {
    val lineColor = Pond.colors.creation
    Column(
        spacingUnits = 0,
        modifier = Modifier.fillMaxWidth()
            .animateItem()
            .padding(Pond.ruler.unitPadding),
    ) {
        PathMapItemPart(
            verticalAlignment = Alignment.Top,
            lineSlot = {
                StepLineSegment(
                    modifier = Modifier.drawBehind {
                        drawStepCircle(lineColor)
                    }
                ) {
                    StepImage(
                        url = null,
                        modifier = Modifier.fillMaxWidth()
                            .padding(StepLineStrokeWidth * 2)
                            .drawLabel("new", alignX = AlignX.Center)
                            .clip(CircleShape)
                    )
                }
            }
        ) {
            EditText(
                text = newStepLabel,
                placeholder = "new step label",
                maxLines = 2,
                style = Pond.typo.h5,
                isContainerVisible = true,
                onAcceptEdit = viewModel::addNewStep,
                modifier = Modifier.weight(1f)
            )
            Column(
                spacingUnits = 0,
            ) {
                Button(
                    imageVector = TablerIcons.ArrowUp,
                    background = Pond.colors.secondary,
                    shape = Pond.ruler.roundTop,
                ) { viewModel.moveNewStep(-1) }
                Button(
                    imageVector = TablerIcons.ArrowDown,
                    background = Pond.colors.secondary,
                    shape = Pond.ruler.roundBottom
                ) { viewModel.moveNewStep(1) }
            }
        }
        Box(
            modifier = stepLineSegmentModifier
                .drawBehind {
                    drawTail(lineColor, !isLastPosition)
                }
        )
    }
}