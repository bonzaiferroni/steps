package ponder.steps.ui

import androidx.compose.runtime.Composable
import ponder.steps.model.data.Step
import pondui.ui.controls.Text

@Composable
fun StepProfileView(step: Step) {
    Text("Profile: ${step.label}")
}