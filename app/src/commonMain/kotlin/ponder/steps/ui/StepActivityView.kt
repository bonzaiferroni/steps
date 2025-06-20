package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.charts.AxisValue
import pondui.ui.charts.ChartArray
import pondui.ui.charts.ChartConfig
import pondui.ui.charts.ChartValue
import pondui.ui.controls.Column
import pondui.ui.charts.LineChart
import pondui.ui.charts.AxisConfig
import pondui.ui.theme.DefaultColors.swatches
import pondui.ui.theme.Pond

@Composable
fun StepActivityView(stepId: String) {
    val viewModel = viewModel { StepActivityModel(stepId) }
    val state by viewModel.state.collectAsState()

    Column(1) {
        LineChart(
            arrays = listOf(
                ChartArray(
                    values = listOf(
                        ChartValue(0f, 0f, "1"),
                        ChartValue(1f, 10f, "2"),
                        ChartValue(2f, 5f, "3"),
                        ChartValue(3f, 30f, "4"),
                        ChartValue(4f, 5f, "5"),
                    ),
                    color = swatches[0]
                ),
                ChartArray(
                    values = listOf(
                        ChartValue(0f, 30f, "1"),
                        ChartValue(1f, 15f, "2"),
                        ChartValue(2f, 7f, "3"),
                        ChartValue(3f, 15f, "4"),
                        ChartValue(4f, 8f, "5"),
                    ),
                    color = swatches[1]
                )
            ),
            config = ChartConfig(
                leftAxis = AxisConfig(
                    values = listOf(AxisValue(0f), AxisValue(15f), AxisValue(30f)),
                    color = swatches[0]
                    ),
                rightAxis = AxisConfig(
                    values = listOf(AxisValue(0f), AxisValue(15f), AxisValue(30f)),
                    color = swatches[1]
                    ),
                bottomAxis = AxisConfig(
                    values = listOf(AxisValue(0f), AxisValue(1f), AxisValue(2f), AxisValue(3f), AxisValue(4f)),
                    color = Pond.localColors.content
                ),
                glowColor = Pond.colors.glow,
                contentColor = Pond.localColors.content
            ),
            modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White.copy(0f))
        )
    }
}