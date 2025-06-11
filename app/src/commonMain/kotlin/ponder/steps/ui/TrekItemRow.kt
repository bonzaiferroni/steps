package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kabinet.utils.pluralize
import ponder.steps.StepProfileRoute
import ponder.steps.model.data.TrekItem
import pondui.ui.behavior.ifNotNull
import pondui.ui.behavior.ifTrue
import pondui.ui.controls.*
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond

@Composable
fun LazyItemScope.TrekItemRow(
    item: TrekItem,
    completeStep: (TrekItem) -> Unit,
) {
    val isFinished = item.finishedAt != null
    val nav = LocalNav.current
    Row(
        spacingUnits = 1,
        modifier = Modifier.fillMaxWidth()
            .animateItem()
            .ifTrue(isFinished) { alpha(.5f) }
    ) {
        Checkbox(item.finishedAt != null) { completeStep(item) }
        StepImage(
            url = item.stepThumbUrl,
            modifier = Modifier.height(72.dp)
                .clip(CircleShape)
                // .ifNotNull(onImageClick) { actionable(onClick = it) }
        )
        Column(0, modifier = Modifier.weight(1f)) {
            Text(
                text = item.stepLabel,
                style = Pond.typo.h4,
                maxLines = 2,
            )
            if (item.intentLabel != item.stepLabel) {
                Row(1) {
                    Label("Path:")
                    Text(item.intentLabel)
                }
            }
        }
        Column(1) {
            val progressRatio = item.stepIndex / item.stepCount.toFloat()
            ProgressBar(progressRatio) {
                Text("${item.stepIndex} of ${item.stepCount}")
            }
        }
    }
}