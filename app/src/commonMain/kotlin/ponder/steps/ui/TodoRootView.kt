package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.collections.immutable.ImmutableList
import ponder.steps.model.data.Tag
import ponder.steps.model.data.TrekId
import pondui.ui.behavior.selected
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.H1
import pondui.ui.controls.LazyRow
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.theme.Pond

@Composable
fun TodoRootView(
    loadTrek: (TrekId?, Boolean) -> Unit,
) {
    val viewModel = viewModel { TodoRootModel(loadTrek) }
    val trekStepsStae by viewModel.treks.state.collectAsState()
    val state by viewModel.state.collectAsState()

    Column(1) {
        Section {
            Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
                H1("Today's Journey")
                ProgressBar(
                    progress = trekStepsStae.progressRatio,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${trekStepsStae.totalProgress} of ${trekStepsStae.totalSteps}")
                }
            }
        }

        TagSetRow(state.selectedTag, state.tagSet, viewModel::clickTag)

        TrekStepListView(viewModel.treks, null)
    }
}

@Composable
fun TagSetRow(
    selectedTag: Tag?,
    tagSet: ImmutableList<Tag>,
    clickTag: (Tag) -> Unit
) {
    LazyRow(1) {
        items(tagSet) { tag ->
            Button(tag.label, Pond.colors.secondary, modifier = Modifier.selected(tag == selectedTag)) { clickTag(tag) }
        }
    }
}