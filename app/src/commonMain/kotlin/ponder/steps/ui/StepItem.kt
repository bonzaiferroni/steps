package ponder.steps.ui

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepPosition
import pondui.ui.behavior.ifNotNull
import pondui.ui.controls.*
import pondui.ui.theme.Pond

@Composable
fun StepItem(
    step: Step,
    isEditable: Boolean = false,
    modifier: Modifier = Modifier,
    onImageClick: (() -> Unit)? = null,
    updateLabel: (String) -> Unit = { },
) {
    StepItem(
        label = step.label,
        thumbUrl = step.thumbUrl,
        position = step.position,
        isEditable = isEditable,
        modifier = modifier,
        onImageClick = onImageClick,
        updateLabel = updateLabel
    )
}

@Composable
fun StepItem(
    label: String,
    thumbUrl: String?,
    position: Int? = null,
    isEditable: Boolean = false,
    modifier: Modifier = Modifier,
    onImageClick: (() -> Unit)? = null,
    updateLabel: (String) -> Unit = { },
) {
    Row(1, modifier = modifier.height(70.dp)) {
        StepImage(
            url = thumbUrl,
            modifier = Modifier.clip(Pond.ruler.pill)
                .ifNotNull(onImageClick) { actionable(onClick = it) }
        )
        position?.let {
            Label("${it + 1}.")
        }
        EditText(
            text = label,
            placeholder = "Step label",
            style = Pond.typo.bodyLarge,
            isEditable = isEditable,
            onAcceptEdit = updateLabel,
            maxLines = 2
        )
    }
}