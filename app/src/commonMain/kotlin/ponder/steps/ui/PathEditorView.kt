package ponder.steps.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.Drone
import compose.icons.tablericons.Plus
import compose.icons.tablericons.QuestionMark
import compose.icons.tablericons.Trash
import ponder.steps.PathEditorRoute
import ponder.steps.model.data.StepId
import pondui.ui.behavior.Magic
import pondui.ui.behavior.magic
import pondui.ui.behavior.padBottom
import pondui.ui.behavior.selected
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.ControlSet
import pondui.ui.controls.ControlSetButton
import pondui.ui.controls.DropMenu
import pondui.ui.controls.EditText
import pondui.ui.controls.Expando
import pondui.ui.controls.Icon
import pondui.ui.controls.IconButton
import pondui.ui.controls.LabeledPart
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.PartLabel
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TextButton
import pondui.ui.controls.actionable
import pondui.ui.controls.bottomBarSpacerItem
import pondui.ui.controls.topBarSpacerItem
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond
import pondui.ui.theme.PondColors
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
        topBarSpacerItem()

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
            var isHovered by remember { mutableStateOf(false) }
            val isSelected = state.selectedStepId == step.id
            val showControls = isSelected || isHovered
            val lineColumnWidth = 72.dp
            val lineWidth = 3.dp
            val lineColor = Pond.colors.swatches[0]
            val animatedLineColor by animateColorAsState(if (isHovered) lineColor else lineColor.darken(.2f))
            val isLastStep = (step.position ?: 0) == pathStep.pathSize - 1
            val questions = state.questions[step.id]
            Column(
                modifier = Modifier.animateItem()
                    .selected(isSelected, radius = Pond.ruler.unitCorner)
                    .actionable(
                        onHover = { isHovered = it },
                        isIndicated = false,
                    ) { viewModel.selectStep(step.id) },
            ) {
                Row(
                    spacingUnits = 1,
                    modifier = Modifier.height(IntrinsicSize.Max)
                ) {
                    // line column
                    Column(
                        modifier = Modifier.width(lineColumnWidth)
                            .fillMaxHeight()
                    ) {
                        StepImage(
                            url = step.thumbUrl,
                            modifier = Modifier.fillMaxWidth()
                                .drawBehind {
                                    drawStepCircle(lineWidth, animatedLineColor)
                                }
                                .padding(lineWidth * 2)
                                .clip(CircleShape)
                        )
                        if (!isLastStep) {
                            LineSection(
                                color = animatedLineColor,
                                width = lineWidth,
                            )
                        }
                    }
                    // text fields
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
                    // position controls
                    ControlSet(
                        maxItemsInEachRow = 1,
                        modifier = Modifier.padding(end = Pond.ruler.unitSpacing)
                            .magic(showControls, scale = .8f)
                    ) {
                        ControlSetButton(
                            imageVector = TablerIcons.ArrowUp,
                            isEnabled = showControls && (step.position ?: 0) > 0,
                            background = Pond.colors.secondary
                        ) { viewModel.moveStep(step, -1) }
                        ControlSetButton(
                            imageVector = TablerIcons.ArrowDown,
                            isEnabled = showControls && (step.position ?: 0) < state.steps.size - 1,
                            background = Pond.colors.secondary
                        ) { viewModel.moveStep(step, 1) }
                    }
                }
                if (step.pathSize > 0) {
                    Row(
                        spacingUnits = 1,
                        modifier = Modifier.height(IntrinsicSize.Max)
                    ) {
                        Box(
                            modifier = Modifier.width(lineColumnWidth)
                                .fillMaxHeight()
                                .branchLine(animatedLineColor, lineWidth, isLastStep)
                        )
                        Text("${step.pathSize} steps")
                        IconButton(TablerIcons.ArrowRight) { nav.go(PathEditorRoute(step.id)) }
                    }
                }
                if (questions != null) {
                    for (question in questions) {
                        Row(
                            spacingUnits = 1,
                            modifier = Modifier.height(IntrinsicSize.Max)
                        ) {
                            // line column
                            Column(
                                modifier = Modifier.fillMaxHeight()
                                    .width(lineColumnWidth),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .drawBehind {
                                            drawStepCircle(lineWidth, animatedLineColor)
                                        }
                                        .padding(lineWidth)
                                ) {
                                    Icon(TablerIcons.QuestionMark)
                                }
                                if (!isLastStep) {
                                    LineSection(animatedLineColor, lineWidth)
                                }
                            }
                            // question controls
                            Column(
                                spacingUnits = 1,
                                modifier = Modifier.padBottom(1)
                            ) {
                                EditText(
                                    text = question.text,
                                    placeholder = "Question text",
                                    isContainerVisible = true,
                                ) { viewModel.editQuestion(question.copy(text = it)) }
                                DropMenu(question.type) { viewModel.editQuestion(question.copy(type = it)) }
                            }
                        }
                    }
                }
                Row(
                    spacingUnits = 1,
                    modifier = Modifier.height(IntrinsicSize.Max)
                ) {
                    // line column
                    Column(
                        modifier = Modifier.fillMaxHeight()
                            .width(lineColumnWidth)
                    ) {
                        if (!isLastStep) {
                            LineSection(animatedLineColor, lineWidth)
                        }
                    }
                    // step controls
                    Row(
                        spacingUnits = 1,
                        modifier = Modifier.padding(bottom = Pond.ruler.unitSpacing)
                            .magic(showControls, scale = .8f)
                    ) {
                        if (step.pathSize == 0) {
                            Button(
                                text = "Add branch",
                                isEnabled = showControls,
                            ) { nav.go(PathEditorRoute(step.id)) }
                        }

                        Button(
                            imageVector = TablerIcons.Trash,
                            isEnabled = showControls,
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

        bottomBarSpacerItem()
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

@Composable
fun Modifier.branchLine(
    color: Color,
    width: Dp,
    isLastStep: Boolean
) = this.drawBehind {
    val w = size.width
    val h = size.height
    val midX = w / 2f
    val midY = h / 2f
    val r = midY                       // radius = half height
    val k = 0.55228475f                // Bézier quarter‑circle constant

    val path = Path().apply {
        moveTo(midX, 0f)                  // top‑middle

        // inward bulge → right point at midX + r
        cubicTo(
            midX, k * r,       // cp1 just below top toward center
            midX + r - k * r, midY,        // cp2 just left of right‑point
            midX + r, midY         // end at right‑middle
        )

        if (!isLastStep) {
            // inward bulge → bottom at midY + r
            cubicTo(
                midX + r - k * r, midY,        // cp1 just above right‑point
                midX, midY + r - k * r, // cp2 just above bottom‑point
                midX, midY + r     // end at bottom‑middle
            )
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = width.toPx(),
            cap = StrokeCap.Round
        )
    )
}

fun DrawScope.drawStepCircle(
    strokeWidth: Dp,
    color: Color,
) {
    val lineWidthPx = strokeWidth.toPx()
    val radius = size.width / 2 - lineWidthPx / 2
    drawCircle(
        color = color,
        radius = radius,
        style = Stroke(width = lineWidthPx)
    )
}