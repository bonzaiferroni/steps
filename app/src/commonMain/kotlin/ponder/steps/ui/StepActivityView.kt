package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pondui.ui.charts.ChartArray
import pondui.ui.charts.ChartConfig
import pondui.ui.controls.Column
import pondui.ui.charts.LineChart
import pondui.ui.charts.AxisSide
import pondui.ui.charts.BottomAxisAutoConfig
import pondui.ui.charts.SideAxisAutoConfig
import pondui.ui.charts.TimeChart
import pondui.ui.charts.TimeChartArray
import pondui.ui.theme.DefaultColors.swatches
import pondui.ui.theme.Pond
import kotlin.time.Duration.Companion.days

@Composable
fun StepActivityView(stepId: String) {
    val viewModel = viewModel { StepActivityModel(stepId) }
    val state by viewModel.state.collectAsState()

    Column(1) {
        TimeChart(
            arrays = listOf(
                TimeChartArray(
                    values = state.buckets,
                    color = swatches[0],
                    provideY = { it.sum.toFloat() },
                    provideX = { it.intervalStart },
                    axis = SideAxisAutoConfig(3, AxisSide.Left)
                ),
                TimeChartArray(
                    values = state.buckets,
                    color = swatches[1],
                    provideY = { it.count.toFloat() },
                    provideX = { it.intervalStart },
                    axis = SideAxisAutoConfig(3, AxisSide.Right)
                )
            ),
            config = ChartConfig(
                glowColor = Pond.colors.glow,
                contentColor = Pond.localColors.content,
                bottomAxis = BottomAxisAutoConfig(5)
            ),
            modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White.copy(.1f))
        )
    }
}

data class ChartValue(
    val x: Instant,
    val y: Float,
)

@Composable
fun TimeChartExample() {
    val now = Clock.System.now()
    TimeChart(
        arrays = listOf(
            TimeChartArray(
                values = listOf(
                    ChartValue(now - 0.days, -10000f),
                    ChartValue(now - 1.days, 1000f),
                    ChartValue(now - 2.days, 5000f),
                    ChartValue(now - 3.days, 3000f),
                    ChartValue(now - 4.days, 5000f),
                ),
                color = swatches[0],
                provideX = { it.x },
                provideY = { it.y },
                axis = SideAxisAutoConfig(3, AxisSide.Left)
            ),
            TimeChartArray(
                values = listOf(
                    ChartValue(now - 0.days, 30000f),
                    ChartValue(now - 1.days, 15000f),
                    ChartValue(now - 2.days, 7000f),
                    ChartValue(now - 3.days, 15000f),
                    ChartValue(now - 4.days, 8000f),
                ),
                color = swatches[1],
                provideX = { it.x },
                provideY = { it.y },
                axis = SideAxisAutoConfig(3, AxisSide.Right)
            )
        ),
        config = ChartConfig(
            glowColor = Pond.colors.glow,
            contentColor = Pond.localColors.content,
            bottomAxis = BottomAxisAutoConfig(5)
        ),
        modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White.copy(.1f))
    )
}