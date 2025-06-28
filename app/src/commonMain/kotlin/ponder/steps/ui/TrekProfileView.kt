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
fun TrekProfileView(
    trekId: TrekId,
    loadTrek: (TrekId?) -> Unit
) {
    val viewModel = viewModel { TrekProfileModel(trekId, loadTrek) }
    val state by viewModel.state.collectAsState()

    val trekStep = state.trek ?: return

    Column(1) {
        Section {
            Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    spacingUnits = 1,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(50.dp)) {
                        IconButton(TablerIcons.ArrowLeft) { loadTrek(trekStep.superId)  }
                    }
                    StepImage(
                        url = trekStep.imgUrl,
                        modifier = Modifier.clip(Pond.ruler.defaultCorners)
                            .width(200.dp)
                    )
                    Box(modifier = Modifier.width(50.dp))
                }

                H1(trekStep.stepLabel)
                ProgressBar(
                    progress = trekStep.progressRatio,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${trekStep.progress} of ${trekStep.pathSize}")
                }
            }
        }

        TrekStepListView(viewModel.treks, trekStep.stepId, loadTrek) // viewModel::branchStep
    }
}