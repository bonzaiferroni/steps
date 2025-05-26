package ponder.steps.ui

import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.*
import pondui.ui.nav.Scaffold
import pondui.ui.theme.Spacing

// Arr! This be the screen that shows all the root steps - the captains of our plan!
@Composable
fun RootStepsScreen(
    viewModel: RootStepsModel = viewModel { RootStepsModel() }
) {
    val state by viewModel.state.collectAsState()

    // Add a cloud dialog for creating new root steps, like a pirate's secret meetin' spot!
    Cloud(state.isAddingStep, viewModel::toggleAddingStep) {
        ControlSet {
            TextField(
                text = state.newStepLabel,
                onTextChange = viewModel::setNewStepLabel,
                placeholder = "Enter step name",
            )
            ControlSetButton("Add", onClick = viewModel::createNewRootStep)
        }
    }

    Scaffold {
        // If we have no root steps, show a message like a lookout with nothin' to report!
        if (state.rootSteps.isEmpty()) {
            Text("Arr! No root steps found. Create some to start yer plan!")
        } else {
            // Display all the root steps, like a list of captains in our pirate fleet!
            LazyColumn(Spacing.Unit) {
                items(state.rootSteps) { step ->
                    Row(Spacing.Unit) {
                        Text(step.label)
                        ControlSet {
                            Button("View Details", onClick = { viewModel.navigateToStep(step) })
                        }
                    }
                }
            }
        }

        // Add a button to create new root steps, like a call to arms for new recruits!
        Button("Add", onClick = viewModel::toggleAddingStep)
    }
}
