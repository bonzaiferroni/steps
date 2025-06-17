package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Plus
import ponder.steps.StepProfileRoute
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekItem
import ponder.steps.model.data.TrekStep
import pondui.ui.behavior.MagicItem
import pondui.ui.behavior.ifTrue
import pondui.ui.behavior.magic
import pondui.ui.controls.Checkbox
import pondui.ui.controls.Column
import pondui.ui.controls.ContentButton
import pondui.ui.controls.IconButton
import pondui.ui.controls.Label
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond

@Composable
fun LazyItemScope.TrekStepRow(
    item: TrekStep,
    isFinished: Boolean,
    isHeader: Boolean,
    isDeeper: Boolean,
    completeStep: (TrekStep, StepOutcome) -> Unit,
    loadTrek: (String) -> Unit,
    branchStep: (String) -> Unit,
) {
    val nav = LocalNav.current
    Row(
        spacingUnits = 1,
        modifier = Modifier.fillMaxWidth()
            .animateItem()
            .magic(offsetX = if (isDeeper) 30.dp else (-30).dp)
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
                    val intentLabel = item.intentLabel
                    if (intentLabel != null && intentLabel != item.stepLabel) {
                        Row(1) {
                            Label("Path:")
                            Text(intentLabel)
                        }
                    }
                }
            }
        }
        if (isHeader) {
            val progressRatio = (item.progress ?: 0) / item.pathSize.toFloat()
            ProgressBar(progressRatio) {
                Text("${item.progress} of ${item.pathSize}")
            }
        } else {
            val trekId = item.trekId
            val pathStepId = item.pathStepId
            if (trekId != null) {
                IconButton(TablerIcons.ArrowRight) { loadTrek(trekId) }
            } else if (pathStepId != null) {
                IconButton(TablerIcons.Plus) { branchStep(pathStepId) }
            }
        }
    }
}