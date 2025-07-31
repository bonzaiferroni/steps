package ponder.steps.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Point
import compose.icons.tablericons.QuestionMark
import ponder.steps.io.StepOutcome
import ponder.steps.model.data.Answer
import ponder.steps.model.data.Question
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepLog
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
import pondui.ui.theme.Pond
import pondui.utils.darken

@Composable
fun LazyItemScope.PathMapStep(
    step: Step,
    isSelected: Boolean,
    isLastStep: Boolean,
    isCompleted: Boolean?,
    progress: Int?,
    questionsAndAnswers: QuestionsAndAnswers,
    currentQuestion: Question?,
    setOutcome: (Step, StepOutcome?) -> Unit,
    onFocusChanged: (FocusState) -> Unit,
    answerQuestion: (String?) -> Unit,
    navToPath: (Step) -> Unit,
) {
    val questions = questionsAndAnswers.questions;
    val answers = questionsAndAnswers.answers

    MagicItem(
        item = currentQuestion,
        offsetX = 50.dp,
        itemContent = { question ->
            QuestionRow(
                stepLabel = step.label,
                question = question,
                answerQuestion = answerQuestion
            )
        },
        isVisibleInit = true,
        modifier = Modifier.animateItem()
    ) {
        var isHovered by remember { mutableStateOf(false) }
        val lineColor = provideStepLineColor(isCompleted, isHovered)
        val animatedLineColor by animateColorAsState(lineColor)
        Column(
            modifier = Modifier.selected(isSelected, radius = Pond.ruler.defaultCorner)
                // .background(Pond.colors.void.copy(.2f))
                .padding(vertical = Pond.ruler.unitSpacing)
                .focusable(onFocusChanged)
                .actionable(
                    onHover = { isHovered = it },
                    isIndicated = false,
                ) { },
        ) {
            PathMapStepHeader(
                step = step,
                progress = progress,
                isCompleted = isCompleted,
                navToPath = navToPath,
                setOutcome = setOutcome,
            ) {
                StepLineCircle(isCompleted, isHovered) {
                    StepImage(
                        url = step.thumbUrl,
                        modifier = Modifier.fillMaxWidth()
                            .padding(StepLineStrokeWidth * 2)
                            .drawLabel("step ${(step.position ?: 0) + 1}", alignX = AlignX.Center)
                            .clip(CircleShape)
                    )
                }
                val isContinued = !isLastStep || step.pathSize > 0 || questions.isNotEmpty()
                StepLineFill(isContinued, isCompleted, isHovered)
            }
            if (step.pathSize > 0) {
                PathMapItemPart(
                    lineSlot = {
                        StepLineBranch(!isLastStep, isCompleted, isHovered)
                    },
                    spacingUnits = 0,
                ) {
                    Row(
                        spacingUnits = 0,
                    ) {
                        val maxStepRowSize = 8
                        repeat(minOf(maxStepRowSize, step.pathSize)) { index ->
                            val alpha = if (index < maxStepRowSize - 2 || step.pathSize <= maxStepRowSize) 1f
                            else if (index == maxStepRowSize - 2) .66f
                            else .33f
                            val lineColor = progress?.let {
                                if (index < it) Pond.colors.data else Pond.colors.void
                            } ?: animatedLineColor
                            Icon(
                                imageVector = TablerIcons.Point,
                                modifier = Modifier.alpha(alpha)
                                    .drawBehind {
                                        drawStepCircle(lineColor)
                                    },
                            )
                        }
                    }
                    // Text("${step.pathSize} steps")
                    IconButton(TablerIcons.ArrowRight) { navToPath(step) }
                }
            }
            for (question in questions) {
                PathMapItemPart(
                    lineSlot = {
                        val isQuestionAnswered = answers?.any { a -> a.questionId == question.id }
                        StepLineCircle(isQuestionAnswered, isHovered) {
                            Icon(TablerIcons.QuestionMark)
                        }
                    }
                ) {
                    // question controls
                    Text(
                        text = question.text,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            StepLineTail(!isLastStep, isCompleted, isHovered)
        }
    }
}

@Stable
data class QuestionsAndAnswers(
    val questions: List<Question>,
    val answers: List<Answer>?,
)

@Composable
fun PathMapStepHeader(
    step: Step,
    progress: Int?,
    isCompleted: Boolean?,
    navToPath: (Step) -> Unit,
    setOutcome: (Step, StepOutcome?) -> Unit,
    lineSlot: @Composable () -> Unit,
) {
    PathMapItemPart(
        verticalAlignment = Alignment.Top,
        lineSlot = lineSlot
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

                if (step.pathSize > 0) {
                    val progress = progress ?: 0
                    val progressRatio = progress / step.pathSize.toFloat()
                    ProgressBarButton(
                        progress = progressRatio,
                        onClick = { navToPath(step) },
                        minWidth = 80.dp
                    ) {
                        Text("$progress of ${step.pathSize}")
                    }
                } else {
                    if (isCompleted != null) {
                        Checkbox(isCompleted, modifier = Modifier.padding(end = 10.dp)) {
                            val outcome = when {
                                isCompleted -> null
                                else -> StepOutcome.Finished
                            }
                            setOutcome(step, outcome)
                        }
                    }
                }
            }
            step.description?.let {
                Text(it)
            }
        }
    }
}
