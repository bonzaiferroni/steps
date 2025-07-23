package ponder.steps.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
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
import pondui.ui.behavior.AlignX
import pondui.ui.behavior.drawLabel
import pondui.ui.behavior.focusable
import pondui.ui.behavior.magic
import pondui.ui.behavior.selected
import pondui.ui.controls.Button
import pondui.ui.controls.Column
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
fun LazyItemScope.PathContextItem(
    step: Step,
    isSelected: Boolean,
    isLastStep: Boolean,
    viewModel: PathContextModel,
) {
    val pathContextState by viewModel.stateFlow.collectAsState()
    val nav = LocalNav.current
    var isHovered by remember { mutableStateOf(false) }
    val showControls = isSelected || isHovered
    val lineColor = Pond.colors.swatches[0]
    val animatedLineColor by animateColorAsState(if (isHovered) lineColor else lineColor.darken(.2f))
    val questions = pathContextState.questions[step.id]
    Column(
        modifier = Modifier.animateItem()
            .selected(isSelected, radius = Pond.ruler.defaultCorner)
            .clip(Pond.ruler.defaultCorners)
            .background(Pond.colors.void.copy(.2f))
            .padding(Pond.ruler.unitSpacing)
            .focusable { state ->
                when (state.isFocused) {
                    true -> viewModel.setFocus(step.id)
                    false -> viewModel.setFocus(null)
                }
            }
            .actionable(
                onHover = { isHovered = it },
                isIndicated = false,
            ) { },
    ) {
        PathMapItemPart(
            verticalAlignment = Alignment.Top,
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
                            .drawLabel("step ${(step.position ?: 0) + 1}", alignX = AlignX.Center)
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
                Text(
                    text = step.label,
                    style = Pond.typo.h3,
                )
                Text(text = step.description ?: "")

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
                }
            }
        }
    }
}