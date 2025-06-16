package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import ponder.steps.StepProfileRoute
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekItem
import pondui.ui.behavior.MagicItem
import pondui.ui.behavior.ifTrue
import pondui.ui.controls.*
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond

@Composable
fun LazyItemScope.TrekItemRow(
    item: TrekItem,
    completeStep: (TrekItem, StepOutcome) -> Unit,
) {
    val isFinished = item.finishedAt != null
    val nav = LocalNav.current
    Row(
        spacingUnits = 1,
        modifier = Modifier.fillMaxWidth()
            .animateItem()
            .ifTrue(isFinished) { alpha(.5f) }
    ) {
        Checkbox(item.finishedAt != null) { completeStep(item, StepOutcome.Completed) }
        MagicItem(
            item = item,
            rotationX = 90,
            modifier = Modifier.weight(1f)
        ) { item ->
            Row(1) {
                ContentButton(
                    onClick = { nav.go(StepProfileRoute(item.stepId)) },
                    shape = CircleShape
                ) {
                    StepImage(
                        url = item.thumbUrl,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
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
            }
        }
        Column(1) {
            val progressRatio = item.progress / item.pathSize.toFloat()
            ProgressBar(progressRatio) {
                Text("${item.progress} of ${item.pathSize}")
            }
        }
    }
}