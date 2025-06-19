package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.AxisValue
import pondui.ui.controls.ChartArray
import pondui.ui.controls.ChartConfig
import pondui.ui.controls.ChartValue
import pondui.ui.controls.Column
import pondui.ui.controls.LineChart
import pondui.ui.controls.AxisConfig

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
                ),
                ChartArray(
                    values = listOf(
                        ChartValue(0f, 20f, "1"),
                        ChartValue(1f, 15f, "2"),
                        ChartValue(2f, 7f, "3"),
                        ChartValue(3f, 15f, "4"),
                        ChartValue(4f, 8f, "5"),
                    ),
                )
            ),
            config = ChartConfig(
                leftAxis = AxisConfig(values = listOf(AxisValue(0f), AxisValue(15f), AxisValue(30f)),),
                rightAxis = AxisConfig(values = listOf(AxisValue(0f), AxisValue(15f), AxisValue(30f)),),
                bottomAxis = AxisConfig(values = listOf(AxisValue(0f), AxisValue(1f), AxisValue(2f), AxisValue(3f), AxisValue(4f)))
            ),
            modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.White.copy(0f))
        )
    }
}