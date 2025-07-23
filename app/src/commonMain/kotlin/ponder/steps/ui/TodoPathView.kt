package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ponder.steps.model.data.StepId
import ponder.steps.model.data.TrekId
import pondui.ui.controls.Column
import pondui.ui.controls.H2
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.theme.Pond

@Composable
fun TodoPathView(
    trekPath: TrekPath,
    isActive: Boolean,
    navToPath: (TrekPath?, Boolean) -> Unit
) {
    val viewModel = viewModel (key = trekPath.key) { TodoPathModel(trekPath, navToPath) }
    val state by viewModel.state.collectAsState()
    val todoList by viewModel.todoList.stateFlow.collectAsState()

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

    val step = state.step ?: return

    Column(1) {
        Section {
            Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
                StepImage(
                    url = step.imgUrl,
                    modifier = Modifier.clip(Pond.ruler.defaultCorners)
                        .width(100.dp)
                )
                // IconButton(TablerIcons.ArrowLeft) { navToDeeperPath(trekStep.superId, false)  }
                H2(step.label)
                ProgressBar(
                    progress = todoList.progressRatio,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("${todoList.progress} of ${step.pathSize}")
                }
            }
        }

        TodoListView(viewModel.todoList, trekPath.pathId) // viewModel::branchStep
    }
}