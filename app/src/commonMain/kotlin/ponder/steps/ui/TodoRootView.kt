package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.steps.model.data.TrekId
import pondui.ui.controls.Column
import pondui.ui.controls.H1
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Section
import pondui.ui.controls.Text

@Composable
fun TodoRootView(
    loadTrek: (TrekId?, Boolean) -> Unit,
) {
    val viewModel = viewModel { TodoRootModel(loadTrek) }
    val state by viewModel.treks.state.collectAsState()

    Column(1) {
        Section {
            Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
                H1("Today's Journey")
                ProgressBar(
                    progress = state.progressRatio,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${state.totalProgress} of ${state.totalSteps}")
                }
            }
        }

        TrekStepListView(viewModel.treks, null)
    }
}