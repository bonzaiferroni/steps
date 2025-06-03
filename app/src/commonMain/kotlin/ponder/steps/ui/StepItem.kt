package ponder.steps.ui

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import ponder.steps.model.data.Step
import pondui.ui.controls.*
import pondui.ui.theme.Pond
import java.awt.Button

@Composable
fun StepItem(
    step: Step,
    modifier: Modifier = Modifier,
) {
    Row(1, modifier = modifier.height(40.dp)) {
        StepImage(step, modifier = Modifier.clip(Pond.ruler.round))
        step.position?.let {
            Label("$it.")
        }
        Text(step.label, modifier = Modifier.weight(1f))
    }
}