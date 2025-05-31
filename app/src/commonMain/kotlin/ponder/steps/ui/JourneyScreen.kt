package ponder.steps.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import pondui.ui.controls.Button
import pondui.ui.controls.FlowRow
import pondui.ui.controls.H2
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Text
import pondui.ui.controls.Scaffold

@Composable
fun JourneyScreen() {
    val viewModel = viewModel { JourneyModel() }
    val state by viewModel.state.collectAsState()

    Scaffold {
        LazyColumn(1) {
            items(state.trekItems, key = { it.trekId} ) { item ->
                FlowRow(1, 2) {
                    val now = Clock.System.now()
                    val minutesToAvailable = (now - item.availableAt).inWholeMinutes

                    // row 1
                    val progress = item.stepIndex / item.stepCount.toFloat()
                    H2(item.intentLabel, modifier = Modifier.weight(1f))
                    if (minutesToAvailable > 0) {
                        Text("Available in $minutesToAvailable minutes", modifier = Modifier.weight(1f))
                        return@FlowRow
                    }
                    val startedAt = item.startedAt
                    if (startedAt == null) {
                        Button("Start") { viewModel.startTrek(item) }
                        return@FlowRow
                    }
                    ProgressBar(progress, modifier = Modifier.weight(1f)) {
                        Text("${item.stepIndex} of ${item.stepCount} steps completed")
                    }

                    // row 2
                    val minutesSinceStart = (now - startedAt).inWholeMinutes
                    val expectedMinutes = item.expectedMinutes ?: 60
                    val minutesRatio = minutesSinceStart / expectedMinutes.toFloat()
                    Text("Current step: ${item.stepLabel}", modifier = Modifier.weight(1f))
                    ProgressBar(minutesRatio, modifier = Modifier.weight(1f)) {
                        Text("$minutesSinceStart of $expectedMinutes minutes")
                    }

                    // row 3
                    Button("Complete Step", modifier = Modifier.weight(1f)) { viewModel.completeStep(item) }
                }
            }
        }
    }
}