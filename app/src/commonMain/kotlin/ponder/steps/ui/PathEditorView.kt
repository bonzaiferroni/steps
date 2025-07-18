package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.Drone
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import ponder.steps.PathEditorRoute
import ponder.steps.StepProfileRoute
import ponder.steps.model.data.StepId
import pondui.ui.behavior.Magic
import pondui.ui.behavior.magic
import pondui.ui.behavior.selected
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.ControlSet
import pondui.ui.controls.ControlSetButton
import pondui.ui.controls.EditText
import pondui.ui.controls.Expando
import pondui.ui.controls.Label
import pondui.ui.controls.LabeledPart
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.PartLabel
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TextButton
import pondui.ui.controls.actionable
import pondui.ui.nav.ContextMenu
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond
import pondui.utils.addShadow
import pondui.utils.darken

@Composable
fun PathEditorView(
    pathId: StepId,
    viewModel: PathEditorModel = viewModel { PathEditorModel() }
) {
    val state by viewModel.state.collectAsState()
    val nav = LocalNav.current

    LaunchedEffect(pathId) {
        viewModel.setParameters(pathId)
    }

    val pathStep = state.step ?: return

    LazyColumn(1, Alignment.CenterHorizontally) {
        item("header") {
            Column(
                spacingUnits = 2,
                modifier = Modifier.animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    spacingUnits = 2,
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    LabeledPart(
                        label = "Image",
                        modifier = Modifier
                            .fillMaxWidth(.33f)
                            .widthIn(max = 200.dp)
                    ) {
                        StepImage(
                            url = pathStep.imgUrl,
                            modifier = Modifier.fillMaxSize()
                                .aspectRatio(1f)
                                .clip(Pond.ruler.unitCorners)
                        )
                    }
                    LabeledPart(
                        label = "Label",
                        modifier = Modifier
                    ) {
                        EditText(
                            text = pathStep.label,
                            placeholder = "Step label",
                            style = Pond.typo.h1.addShadow(),
                            isContainerVisible = true,
                        ) { viewModel.editStep(pathStep.copy(label = it)) }
                    }
                }
                Magic(pathStep.description == null) {
                    Row(1) {
                        Button("Add description", onClick = viewModel::addDescription)
                    }
                }
                Magic(pathStep.description != null) {
                    LabeledPart("Description") {
                        EditText(
                            text = pathStep.description ?: "",
                            placeholder = "Description",
                            modifier = Modifier.fillMaxWidth(),
                            isContainerVisible = true,
                        ) { viewModel.editStep(pathStep.copy(description = it)) }
                    }
                }
                PartLabel("Steps") {
                    Text("ey")
                }
            }
        }

        itemsIndexed(state.steps, key = { index, step -> step.pathStepId ?: step.id }) { index, step ->
            val isSelected = state.selectedStepId == step.id
            val leftSectionWidth = 72.dp
            val lineWidth = 3.dp
            val lineColor = Pond.colors.swatches[0].darken(.2f)
            val isLastStep = (step.position ?: 0) == pathStep.pathSize - 1
            Column(
                modifier = Modifier.animateItem()
                    .selected(isSelected, radius = Pond.ruler.unitCorner)
                    .actionable { viewModel.selectStep(step.id) },
            ) {
                Row(
                    spacingUnits = 1,
                    modifier = Modifier.height(IntrinsicSize.Max)
                ) {
                    Column(
                        modifier = Modifier.width(leftSectionWidth)
                            .fillMaxHeight()
                    ) {
                        StepImage(
                            url = step.thumbUrl,
                            modifier = Modifier.fillMaxWidth()
                                .drawBehind {
                                    val lineWidthPx = lineWidth.toPx()
                                    val radius = size.width / 2 - lineWidthPx / 2
                                    drawCircle(
                                        color = lineColor,
                                        radius = radius,
                                        style = Stroke(width = lineWidth.toPx())
                                    )
                                }
                                .padding(lineWidth * 2)
                                .clip(CircleShape)
                        )
                        if (!isLastStep) {
                            LineSection(
                                color = lineColor,
                                width = lineWidth,
                            )
                        }
                    }
                    Column(
                        spacingUnits = 1,
                        modifier = Modifier.weight(1f)
                            .padding(bottom = Pond.ruler.unitSpacing)
                    ) {
                        EditText(
                            text = step.label,
                            placeholder = "Step Label",
                            maxLines = 2,
                            style = Pond.typo.h5,
                            isContainerVisible = true,
                        ) { viewModel.editStep(step.copy(label = it)) }
                        EditText(
                            text = step.description ?: "",
                            placeholder = "Step Description",
                            modifier = Modifier.fillMaxWidth(),
                            isContainerVisible = true,
                        ) { viewModel.editStep(step.copy(description = it)) }

                    }
                    ControlSet(
                        maxItemsInEachRow = 1,
                        modifier = Modifier.padding(end = Pond.ruler.unitSpacing)
                            .magic(isSelected, scale = .8f)
                    ) {
                        ControlSetButton(
                            imageVector = TablerIcons.ArrowUp,
                            isEnabled = isSelected && (step.position ?: 0) > 0,
                            background = Pond.colors.secondary
                        ) { viewModel.moveStep(step, -1) }
                        ControlSetButton(
                            imageVector = TablerIcons.ArrowDown,
                            isEnabled = isSelected && (step.position ?: 0) < state.steps.size - 1,
                            background = Pond.colors.secondary
                        ) { viewModel.moveStep(step, 1) }
                    }
                }
                Row(
                    spacingUnits = 1,
                    modifier = Modifier.height(IntrinsicSize.Max)
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight()
                            .width(leftSectionWidth)
                    ) {
                        if (!isLastStep) {
                            LineSection(lineColor, lineWidth)
                        }
                    }
                    Row(
                        spacingUnits = 1,
                        modifier = Modifier.padding(bottom = Pond.ruler.unitSpacing)
                            .magic(isSelected, scale = .8f)
                    ) {
                        if (step.pathSize == 0) {
                            Button(
                                text = "Create path",
                                isEnabled = isSelected,
                            ) { nav.go(PathEditorRoute(step.id)) }
                        }

                        Button(
                            imageVector = TablerIcons.Trash,
                            isEnabled = isSelected,
                            background = Pond.colors.danger,
                        ) { viewModel.removeStepFromPath(step) }
                    }
                }
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
    }
}

@Composable
fun ColumnScope.LineSection(
    color: Color,
    width: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .weight(1f)
            .drawBehind {
                val x = size.width / 2
                drawLine(
                    color = color,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = width.toPx(),
                    cap = StrokeCap.Round
                )
            }
    )
}