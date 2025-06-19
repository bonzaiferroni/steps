package ponder.steps.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StepActivityView(stepId: String) {
    val viewModel = viewModel { StepActivityModel(stepId) }
    val state by viewModel.state.collectAsState()


}