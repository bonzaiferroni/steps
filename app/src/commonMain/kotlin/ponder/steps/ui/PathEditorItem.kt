package ponder.steps.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowDown
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.ArrowUp
import compose.icons.tablericons.Edit
import compose.icons.tablericons.QuestionMark
import compose.icons.tablericons.Trash
import ponder.steps.PathEditorRoute
import ponder.steps.model.data.Step
import pondui.ui.behavior.focusable
import pondui.ui.behavior.magic
import pondui.ui.behavior.selected
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.ControlSet
import pondui.ui.controls.ControlSetButton
import pondui.ui.controls.EditText
import pondui.ui.controls.Icon
import pondui.ui.controls.IconButton
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.actionable
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond
import pondui.utils.darken

@Composable
fun LazyItemScope.PathEditorItem(
    step: Step,
    isSelected: Boolean,
    isLastStep: Boolean,
    viewModel: PathEditorModel,
) {
    val pathContextState by viewModel.pathContextFlow.collectAsState()
    val nav = LocalNav.current
    var isHovered by remember { mutableStateOf(false) }
    val showControls = isSelected || isHovered
    val lineColor = Pond.colors.swatches[0]
    val animatedLineColor by animateColorAsState(if (isHovered) lineColor else lineColor.darken(.2f))
    val questions = pathContextState.questions[step.id]
    Column(
        modifier = Modifier.animateItem()
            .focusable { state ->
                when (state.isFocused) {
                    true -> viewModel.setFocus(step.id)
                    false -> viewModel.setFocus(null)
                }
            }
            .selected(isSelected, radius = Pond.ruler.unitCorner)
            .actionable(
                onHover = { isHovered = it },
                isIndicated = false,
            ) { },
    ) {
        PathMapItemPart(
            lineSlot = {
                StepLineSegment(
                    modifier = Modifier.drawBehind {
                        drawStepCircle(animatedLineColor)
                    }
                ) {
                    StepImage(
                        url = step.thumbUrl,
                        modifier = Modifier.fillMaxWidth()
                            .padding(StepLineStrokeWidth * 2)
                            .clip(CircleShape)
                    )
                }
                val isContinued = !isLastStep || step.pathSize > 0 || questions != null
                StepLineFiller(modifier = Modifier.drawBehind {
                    drawStepLine(animatedLineColor, isContinued)
                })
            }
        ) {
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
                    isEnabled = showControls && (step.position ?: 0) < pathContextState.steps.size - 1,
                    background = Pond.colors.secondary
                ) { viewModel.moveStep(step, 1) }
            }
        }
        if (step.pathSize > 0) {
            PathMapItemPart(
                lineSlot = {
                    StepLineFiller(modifier = Modifier.drawBehind {
                        drawStepBranch(animatedLineColor, isLastStep)
                    })
                }
            ) {
                Text("${step.pathSize} steps")
                IconButton(TablerIcons.ArrowRight) { nav.go(PathEditorRoute(step.id)) }
            }
        }
        if (questions != null) {
            for (question in questions) {
                PathMapItemPart(
                    lineSlot = {
                        StepLineFiller(
                            modifier = Modifier.drawBehind {
                                drawStepCircle(animatedLineColor)
                            }
                                .padding(StepLineStrokeWidth)
                        ) {
                            Icon(TablerIcons.QuestionMark)
                        }
                    }
                ) {
                    // question controls
                    Text(question.text, modifier = Modifier.weight(1f))
                    IconButton(TablerIcons.Edit) { viewModel.setEditQuestion(question) }
                }
            }
        }
        PathMapItemPart(
            lineSlot = {
                StepLineFiller(modifier = Modifier.drawBehind {
                    drawStepLine(animatedLineColor, !isLastStep)
                })
            }
        ) {
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
