package ponder.steps.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.steps.model.data.TrekId

@Composable
fun TodoRootView(
    loadTrek: (TrekId?) -> Unit,
) {
    val viewModel = viewModel { TodoRootModel() }

    TrekStepListView(viewModel.treks, null, loadTrek)
}