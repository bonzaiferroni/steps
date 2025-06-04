package ponder.steps.ui

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ponder.steps.model.data.Step
import pondui.ui.controls.*
import pondui.ui.theme.Pond

@Composable
fun StepItem(
    step: Step,
    isEditable: Boolean = false,
    modifier: Modifier = Modifier,
    updateLabel: (String) -> Unit = { },
) {
    Row(1, modifier = modifier.height(40.dp)) {
        StepImage(step, modifier = Modifier.clip(Pond.ruler.pill))
        step.position?.let {
            Label("${it + 1}.")
        }
        EditText(step.label, isEditable = isEditable, onAcceptEdit = updateLabel)
    }
}