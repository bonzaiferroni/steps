package ponder.steps.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Point
import compose.icons.tablericons.QuestionMark
import ponder.steps.StepProfileRoute
import ponder.steps.db.TrekPointId
import ponder.steps.io.StepOutcome
import ponder.steps.model.data.Step
import pondui.ui.behavior.AlignX
import pondui.ui.behavior.MagicItem
import pondui.ui.behavior.drawLabel
import pondui.ui.behavior.focusable
import pondui.ui.behavior.selected
import pondui.ui.controls.Checkbox
import pondui.ui.controls.Column
import pondui.ui.controls.Icon
import pondui.ui.controls.IconButton
import pondui.ui.controls.ProgressBarButton
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.actionable
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond
import pondui.utils.darken

@Composable
fun LazyItemScope.PathMapStep(
    step: Step,
    trekPointId: TrekPointId?,
    isSelected: Boolean,
    isLastStep: Boolean,
    viewModel: PathContextModel,
    navToPath: ((TrekPointId, Step) -> Unit)?,
) {
    val state by viewModel.stateFlow.collectAsState()
    val log = state.getLog(step)
    val progress = state.getProgress(step)
    val questions = state.questions[step.id] ?: emptyList()
    val answers = log?.let { state.getAnswers(it.id) } ?: emptyList()
    val question = log?.let { questions.firstOrNull { q -> answers.all { a -> a.questionId != q.id } } }
    val isCompleted = log != null

    MagicItem(
        item = question,
        offsetX = 50.dp,
        itemContent = { question ->
            QuestionRow(step.label, question) { answerText ->
                if (trekPointId != null && log != null)
                    viewModel.answerQuestion(step, log, question, answerText)
            }
        },
        isVisibleInit = true,
        modifier = Modifier.animateItem()
    ) {
        val nav = LocalNav.current
        var isHovered by remember { mutableStateOf(false) }
        val showControls = isSelected || isHovered
        val swatchColor = Pond.colors.swatches[0]
        val lineColor = when {
            trekPointId != null -> when {
                log != null -> Pond.colors.data
                else -> Pond.colors.void
            }
            else -> when (isHovered) {
                true -> swatchColor
                else -> swatchColor.darken(.2f)
            }
        }
        val animatedLineColor by animateColorAsState(lineColor)
        Column(
            modifier = Modifier.selected(isSelected, radius = Pond.ruler.defaultCorner)
                // .background(Pond.colors.void.copy(.2f))
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
                    Row(1, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = step.label,
                            style = Pond.typo.h3,
                            modifier = Modifier.weight(1f)
                        )

                        if (progress != null) {
                            val progressRatio = progress / step.pathSize.toFloat()
                            ProgressBarButton(
                                progress = progressRatio,
                                onClick = {
                                    val trekPointId = trekPointId ?: return@ProgressBarButton
                                    val navToPath = navToPath ?: return@ProgressBarButton
                                    navToPath(trekPointId, step)
                                }
                            ) {
                                Row(1) {
                                    Text("$progress of ${step.pathSize}")
                                    Icon(TablerIcons.ArrowRight)
                                }
                            }
                        } else {
                            Checkbox(isCompleted, modifier = Modifier.padding(end = 10.dp)) {
                                val outcome = when {
                                    isCompleted -> null
                                    else -> StepOutcome.Finished
                                }
                                viewModel.setOutcome(step, outcome)
                            }
                        }
                    }
                    Text(text = step.description ?: "")

                }

            }
            if (step.pathSize > 0) {
                PathMapItemPart(
                    lineSlot = {
                        StepLineFiller(modifier = Modifier.drawBehind {
                            drawStepBranch(animatedLineColor, isLastStep)
                        })
                    },
                    spacingUnits = 0,
                ) {
                    Row(
                        spacingUnits = 0,
                    ) {
                        repeat(minOf(8, step.pathSize)) {
                            Icon(
                                imageVector = TablerIcons.Point,
                                modifier = Modifier.drawBehind {
                                    drawStepCircle(animatedLineColor)
                                },
                            )
                        }
                    }
                    // Text("${step.pathSize} steps")
                    IconButton(TablerIcons.ArrowRight) { nav.go(StepProfileRoute(step.id)) }
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
            Box(
                modifier = stepLineSegmentModifier
                    .drawBehind {
                        drawTail(animatedLineColor, !isLastStep)
                    }
            )
        }
    }
}