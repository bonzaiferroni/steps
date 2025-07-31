package ponder.steps.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import pondui.ui.theme.Pond


@Composable
fun StepLineCircle(
    isComplete: Boolean? = null,
    isHighlighted: Boolean = false,
    content: @Composable () -> Unit
) = StepLineCircle(
    color = provideStepLineColor(isCompleted = isComplete, isHighlighted = isHighlighted),
    content = content
)

@Composable
fun StepLineCircle(
    color: Color,
    content: @Composable () -> Unit
) {
    val animatedColor by animateColorAsState(color)

    StepLineSegment(
        modifier = Modifier.drawBehind {
            drawStepCircle(animatedColor)
        }
            .padding(StepLineStrokeWidth * 2)
    ) {
        content()
    }
}

@Composable
fun ColumnScope.StepLineFill(
    isContinued: Boolean,
    isCompleted: Boolean? = null,
    isHighlighted: Boolean = false,
    content: (@Composable () -> Unit)? = null
) {
    if (!isContinued) return

    val lineColor by animateColorAsState(provideStepLineColor(isCompleted, isHighlighted))

    StepLineSegment(
        modifier = stepLineSegmentModifier.weight(1f)
            .drawBehind {
                drawStepLine(lineColor, true)
            },
        content = content
    )
}

@Composable
fun StepLineTail(
    isContinued: Boolean,
    isCompleted: Boolean? = null,
    isHighlighted: Boolean = false,
) {
    if (!isContinued) return
    val lineColor = provideStepLineColor(isCompleted, isHighlighted)
    StepLineTail(lineColor)
}

@Composable
fun StepLineTail(
    color: Color
) {
    val animatedColor by animateColorAsState(color)

    Box(
        modifier = stepLineSegmentModifier
            .drawBehind {
                drawTail(animatedColor, true)
            }
    )
}

@Composable
fun ColumnScope.StepLineBranch(
    isContinued: Boolean,
    isCompleted: Boolean? = null,
    isHighlighted: Boolean = false,
) {
    val lineColor by animateColorAsState(provideStepLineColor(isCompleted, isHighlighted))

    StepLineSegment(
        modifier = stepLineSegmentModifier.weight(1f)
            .drawBehind {
                drawStepBranch(lineColor, isContinued)
            },
    )
}