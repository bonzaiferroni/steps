package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.toDoubleMillis
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pondui.ui.charts.ChartArray
import pondui.ui.charts.ChartConfig
import pondui.ui.controls.Column
import pondui.ui.charts.AxisSide
import pondui.ui.charts.BottomAxisAutoConfig
import pondui.ui.charts.ChartBox
import pondui.ui.charts.SideAxisAutoConfig
import pondui.ui.charts.TimeChart
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Expando
import pondui.ui.theme.DefaultColors.swatches
import pondui.ui.theme.Pond
import kotlin.time.Duration.Companion.hours

@Composable
fun StepActivityView(stepId: String) {
    val viewModel = viewModel { StepActivityModel(stepId) }
    val state by viewModel.state.collectAsState()

    LazyColumn(1) {
        item {
            ChartBox("Step completions") {
                TimeChart(
                    arrays = listOf(
                        ChartArray(
                            values = state.countBuckets,
                            color = swatches[0],
                            provideY = { it.count.toDouble() },
                            axis = SideAxisAutoConfig(3, AxisSide.Left),
                            floor = 0.0,
                        )
                    ),
                    config = ChartConfig(
                        glowColor = Pond.colors.glow,
                        contentColor = Pond.localColors.content,
                        bottomAxis = BottomAxisAutoConfig(5),
                    ),
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    provideX = { it.intervalStart }
                )
                Expando(2)
            }
        }

        items(state.intQuestionBuckets) { intQuestionBucket ->
            ChartBox(intQuestionBucket.question.text) {
                TimeChart(
                    arrays = listOf(
                        ChartArray(
                            values = intQuestionBucket.buckets,
                            color = swatches[0],
                            provideY = { it.sum.toDouble() },
                            axis = SideAxisAutoConfig(3, AxisSide.Left),
                            floor = 0.0
                        ),
                        ChartArray(
                            values = intQuestionBucket.buckets,
                            color = swatches[1],
                            provideY = { it.count.toDouble() },
                            axis = SideAxisAutoConfig(3, AxisSide.Right),
                            floor = 0.0,
                        )
                    ),
                    config = ChartConfig(
                        glowColor = Pond.colors.glow,
                        contentColor = Pond.localColors.content,
                        startX = (Clock.System.now() - 6.hours).toDoubleMillis(),
                        bottomAxis = BottomAxisAutoConfig(5),
                    ),
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    provideX = { it.intervalStart },
                )
            }
        }
    }
}

data class ChartValue(
    val x: Instant,
    val y: Float,
)