package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TodoPathView(
    trekPath: TrekPath,
    isActive: Boolean,
    navToPath: (TrekPath?, Boolean) -> Unit
) {
    val viewModel = viewModel (key = trekPath.key) { TodoPathModel(trekPath, navToPath) }
    val state by viewModel.stateFlow.collectAsState()

    LaunchedEffect(isActive) {
        if (isActive) {
            viewModel.activate()
        } else {
            viewModel.deactivate()
        }
    }

    // Notify on leave
    DisposableEffect(Unit) {
        onDispose {
            viewModel.deactivate()
        }
    }

    // val step = state.step ?: return

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        PathMapView(viewModel.pathContext) {
            navToPath(trekPath.toExtendedPath(it), true)
        }
    }
}