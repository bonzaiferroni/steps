package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.utils.toAgoDescription
import kotlinx.datetime.Clock
import pondui.ui.behavior.Magic
import pondui.ui.behavior.magic
import pondui.ui.controls.*
import pondui.ui.theme.Pond
import kotlin.time.Duration.Companion.seconds

@Composable
fun TrekListView() {
    val viewModel = viewModel { TrekListModel() }
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose(viewModel::onDispose)
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad()
    }

    LazyColumn(1) {
        if (state.treks.isEmpty()) {
            item {
                Text("No treks!")
            }
        }
        items(state.treks, key = { it.trekId }) { item ->
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
                        if (finishedAt != null && now > finishedAt + 10.seconds) {
                            Text("Finished ${(now - finishedAt).toAgoDescription()}")
                            return@Box
                        }
                        if (availableIn.isPositive()) {
                            Text("Available ${(-availableIn).toAgoDescription()}")
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

                Magic(startedAt != null && finishedAt == null, rotationX = 90) {
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
                        val completeButtonText = if (item.stepIndex + 1 == item.stepCount) "Complete Trek"
                        else "Complete Step"
                        Button(
                            text = completeButtonText,
                            modifier = Modifier.weight(1f)
                        ) { viewModel.completeStep(item) }
                        Button(
                            "Step into", background = Pond.colors.secondary,
                            modifier = Modifier.weight(1f).magic(item.stepPathSize > 0, rotationX = 90)
                        ) { viewModel.stepIntoPath(item) }
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