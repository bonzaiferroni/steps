package ponder.steps.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.Button
import pondui.ui.controls.Controls
import pondui.ui.controls.Text
import pondui.ui.nav.Scaffold

// Arr! This be the screen that shows all the root steps - the captains of our plan!
@Composable
fun RootStepsScreen(
    viewModel: RootStepsModel = viewModel { RootStepsModel() }
) {
    val state by viewModel.state.collectAsState()
    Scaffold {
        // If we have no root steps, show a message like a lookout with nothin' to report!
        if (state.rootSteps.isEmpty()) {
            Text("Arr! No root steps found. Create some to start yer plan!")
        } else {
            // Display all the root steps, like a list of captains in our pirate fleet!
            LazyColumn {
                items(state.rootSteps) { step ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(step.label)
                        Controls {
                            Button("View Details", onClick = { viewModel.navigateToStep(step) })
                        }
                    }
                }
            }
        }
    }
}