package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val StepLineColumnWidth = 72.dp
val StepLineStrokeWidth = 3.dp

private val stepLineSegmentModifier = Modifier.width(StepLineColumnWidth)

@Composable
fun StepLineSegment(
    modifier: Modifier = Modifier,
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = stepLineSegmentModifier.then(modifier),
        content = content ?: { },
        contentAlignment = Alignment.Center
    )
}

@Composable
fun ColumnScope.StepLineFiller(
    modifier: Modifier = Modifier,
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    StepLineSegment(
        modifier = stepLineSegmentModifier.weight(1f).then(modifier),
        content = content
    )
}

fun DrawScope.drawStepLine(
    color: Color,
    isContinued: Boolean
) {
    if (!isContinued) return

    val x = size.width / 2
    drawLine(
        color = color,
        start = Offset(x, 0f),
        end = Offset(x, size.height),
        strokeWidth = StepLineStrokeWidth.toPx(),
        cap = StrokeCap.Round
    )
}

fun DrawScope.drawStepBranch(
    color: Color,
    isLastStep: Boolean
) {
    val w = size.width
    val h = size.height
    val midX = w / 2f
    val midY = h / 2f
    val r = midY                                // radius = half height
    val k = 0.5522847f                          // Bézier quarter‑circle constant

    val path = Path().apply {
        moveTo(midX, 0f)                        // top‑middle

        // inward bulge → right point at midX + r
        cubicTo(
            midX, k * r,                        // cp1 just below top toward center
            midX + r - k * r, midY,             // cp2 just left of right‑point
            midX + r, midY                      // end at right‑middle
        )

        if (!isLastStep) {
            // inward bulge → bottom at midY + r
            cubicTo(
                midX + r - k * r, midY,         // cp1 just above right‑point
                midX, midY + r - k * r,         // cp2 just above bottom‑point
                midX, midY + r                  // end at bottom‑middle
            )
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = StepLineStrokeWidth.toPx(),
            cap = StrokeCap.Round
        )
    )
}

fun DrawScope.drawStepCircle(
    color: Color,
) {
    val lineWidthPx = StepLineStrokeWidth.toPx()
    val radius = size.height / 2 - lineWidthPx / 2
    drawCircle(
        color = color,
        radius = radius,
        style = Stroke(width = lineWidthPx)
    )
}