package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.formatSpanLong
import kotlinx.datetime.Clock
import pondui.ui.behavior.FadeIn
import pondui.ui.controls.*
import pondui.ui.theme.Pond

@Composable
fun JourneyScreen() {
    val viewModel = viewModel { JourneyModel() }
    val state by viewModel.state.collectAsState()

    Scaffold {
        LazyColumn(1, modifier = Modifier.weight(1f)) {
            items(state.trekItems, key = { it.trekId }) { item ->
                val startedAt = item.startedAt
                val finishedAt = item.finishedAt
                val now = Clock.System.now()
                Column(1, modifier = Modifier.animateItem().heightIn(min = 50.dp)) {
                    FlowRow(1, 2, Alignment.CenterVertically, modifier = Modifier) {
                        val availableIn = item.availableAt - now

                        // row 1
                        val progress = item.stepIndex / item.stepCount.toFloat()
                        H2(item.intentLabel, modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.weight(1f)) {
                            if (finishedAt != null) {
                                Text("Finished ${(now - finishedAt).formatSpanLong()}")
                                return@Box
                            }
                            if (availableIn.isPositive()) {
                                Text("Available ${(-availableIn).formatSpanLong()}")
                                return@Box
                            }
                            if (startedAt == null) {
                                Button("Start") { viewModel.startTrek(item) }
                                return@Box
                            }
                            ProgressBar(progress, modifier = Modifier.fillMaxWidth()) {
                                Text("${item.stepIndex} of ${item.stepCount} steps completed")
                            }
                        }
                    }

                    FadeIn(startedAt != null && finishedAt == null, rotationX = 90) {
                        FlowRow(1, 2) {
                            // row 2
                            val minutesSinceStart = (now - (startedAt ?: now)).inWholeMinutes
                            val expectedMinutes = item.expectedMinutes ?: 60
                            val minutesRatio = minutesSinceStart / expectedMinutes.toFloat()
                            Text("Current step: ${item.stepLabel}", modifier = Modifier.weight(1f))
                            ProgressBar(minutesRatio, modifier = Modifier.weight(1f)) {
                                Text("$minutesSinceStart of $expectedMinutes minutes")
                            }

                            // row 3
                            Button(
                                text = "Complete Step",
                                modifier = Modifier.weight(1f)
                            ) { viewModel.completeStep(item) }
                            Button(
                                text = "Pause", background = Pond.colors.secondary,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.pauseTrek(item) }
                        }
                    }
                }
            }
        }
    }
}