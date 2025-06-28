package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import ponder.steps.model.data.TrekId
import pondui.ui.controls.Column
import pondui.ui.controls.H1
import pondui.ui.controls.IconButton
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.theme.Pond

@Composable
fun TrekFocusView(
    trekId: TrekId,
    loadTrek: (TrekId?, Boolean) -> Unit
) {
    val viewModel = viewModel (key = trekId) { TrekFocusModel(trekId, loadTrek) }
    val state by viewModel.state.collectAsState()

    val trekStep = state.trek ?: return

    Column(1) {
        Section {
            Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
                StepImage(
                    url = trekStep.imgUrl,
                    modifier = Modifier.clip(Pond.ruler.defaultCorners)
                        .width(200.dp)
                )
                // IconButton(TablerIcons.ArrowLeft) { loadTrek(trekStep.superId, false)  }
                H1(trekStep.stepLabel)
                ProgressBar(
                    progress = trekStep.progressRatio,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${trekStep.progress} of ${trekStep.pathSize}")
                }
            }
        }

        TrekStepListView(viewModel.treks, trekStep.stepId) // viewModel::branchStep
    }
}