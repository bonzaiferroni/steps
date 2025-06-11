package ponder.steps.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kabinet.utils.pluralize
import ponder.steps.model.data.Step
import pondui.ui.behavior.ifNotNull
import pondui.ui.controls.*
import pondui.ui.theme.Pond

@Composable
fun StepRow(
    step: Step,
    isEditable: Boolean = false,
    modifier: Modifier = Modifier,
    onImageClick: (() -> Unit)? = null,
    updateLabel: (String) -> Unit = { },
) {
    StepRow(
        label = step.label,
        thumbUrl = step.thumbUrl,
        position = step.position,
        pathSize = step.pathSize,
        isEditable = isEditable,
        modifier = modifier,
        onImageClick = onImageClick,
        updateLabel = updateLabel
    )
}

@Composable
fun StepRow(
    label: String,
    thumbUrl: String?,
    position: Int? = null,
    isEditable: Boolean = false,
    pathSize: Int = 0,
    modifier: Modifier = Modifier,
    onImageClick: (() -> Unit)? = null,
    updateLabel: (String) -> Unit = { },
) {
    Row(1, modifier = modifier) {
        StepImage(
            url = thumbUrl,
            modifier = Modifier.height(72.dp)
                .clip(CircleShape)
                .ifNotNull(onImageClick) { actionable(onClick = it) }
        )
        position?.let {
            Label("${it + 1}.", Pond.typo.h3)
        }
        Column(0) {
            EditText(
                text = label,
                placeholder = "Step label",
                style = Pond.typo.h3,
                isEditable = isEditable,
                onAcceptEdit = updateLabel,
                maxLines = 2,
            )
            if (pathSize > 0) {
                Label("$pathSize step${pluralize(pathSize)}", maxLines = 1)
            }
        }
    }
}