package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.charts.AxisConfig
import pondui.ui.charts.ChartArray
import pondui.ui.charts.ChartConfig
import pondui.ui.charts.ChartValue
import pondui.ui.controls.Column
import pondui.ui.charts.LineChart
import pondui.ui.charts.AxisSide
import pondui.ui.charts.BottomAxisConfig
import pondui.ui.charts.SideAxisConfig
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
                        ChartValue(0f, -10000f, "1"),
                        ChartValue(1f, 1000f, "2"),
                        ChartValue(2f, 5000f, "3"),
                        ChartValue(3f, 3000f, "4"),
                        ChartValue(4f, 5000f, "5"),
                    ),
                    color = swatches[0],
                    provideX = { it.x },
                    provideY = { it.y },
                    axis = SideAxisConfig(3, AxisSide.Left)
                ),
                ChartArray(
                    values = listOf(
                        ChartValue(0f, 30000f, "1"),
                        ChartValue(1f, 15000f, "2"),
                        ChartValue(2f, 7000f, "3"),
                        ChartValue(3f, 15000f, "4"),
                        ChartValue(4f, 8000f, "5"),
                    ),
                    color = swatches[1],
                    provideX = { it.x },
                    provideY = { it.y },
                    axis = SideAxisConfig(3, AxisSide.Right)
                )
            ),
            config = ChartConfig(
//                leftAxis = AxisConfig(
//                    values = listOf(AxisValue(0f), AxisValue(15f), AxisValue(30f)),
//                    color = swatches[0]
//                    ),
//                rightAxis = AxisConfig(
//                    values = listOf(AxisValue(0f), AxisValue(15f), AxisValue(30f)),
//                    color = swatches[1]
//                    ),
//                bottomAxis = AxisConfig(
//                    values = listOf(AxisValue(0f), AxisValue(1f), AxisValue(2f), AxisValue(3f), AxisValue(4f)),
//                    color = Pond.localColors.content
//                ),
                glowColor = Pond.colors.glow,
                contentColor = Pond.localColors.content,
                bottomAxis = BottomAxisConfig(5)
            ),
            modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White.copy(.1f))
        )
    }
}