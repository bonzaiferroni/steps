package ponder.steps.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Plus
import ponder.steps.StepProfileRoute
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekStep
import pondui.ui.behavior.MagicItem
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
    isDeeper: Boolean,
    questionCount: Int,
    setOutcome: (TrekStep, StepOutcome?) -> Unit,
    loadTrek: (String) -> Unit,
    branchStep: (String) -> Unit,
) {
    val nav = LocalNav.current
    val progress = item.progress
    val showProgress = progress != null && item.pathSize > 0
    val isFinishedAnimated by animateFloatAsState(if (isFinished) 1f else 0f)

    Row(
        spacingUnits = 1,
        modifier = Modifier.fillMaxWidth()
            .animateItem()
            .magic(offsetX = if (isDeeper) 30.dp else (-30).dp)
    ) {
        Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.Center) {
            val trekId = item.trekId
            if (trekId != null && item.pathSize > 0) {
                IconButton(TablerIcons.ArrowRight, padding = PaddingValues(0.dp)) { loadTrek(trekId) }
            } else {
                Checkbox(isFinished) {
                    setOutcome(item, if (isFinished) null else StepOutcome.Completed)
                }
            }
        }
        Row(
            spacingUnits = 1,
            modifier = Modifier.graphicsLayer { alpha = (1f - isFinishedAnimated) * .5f + .5f }
        ) {
            ContentButton(
                onClick = { nav.go(StepProfileRoute(item.stepId)) },
                shape = CircleShape
            ) {
                MagicItem(item.thumbUrl, rotationX = 90) { url ->
                    StepImage(
                        url = url,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
            Row(1, modifier = Modifier.weight(1f)) {
                item.position?.let { Label("${it + 1}.", Pond.typo.bodyLarge) }
                Column(0) {
                    Text(
                        text = item.stepLabel,
                        style = Pond.typo.bodyLarge,
                        maxLines = 2,
                    )
                    if (!showProgress && item.pathSize > 0) {
                        Label("${item.pathSize} steps")
                    }
                    if (questionCount > 0) {
                        Label("$questionCount questions")
                    }
                    val intentLabel = item.intentLabel
                    if (intentLabel != null && intentLabel != item.stepLabel) {
                        Row(1) {
                            Label("Path:")
                            Text(intentLabel)
                        }
                    }
                }
            }
            val pathStepId = item.pathStepId
            if (item.trekId == null && pathStepId != null) {
                IconButton(TablerIcons.Plus) { branchStep(pathStepId) }
            }

            if (showProgress) {
                val progressRatio = progress / item.pathSize.toFloat()
                ProgressBar(progressRatio) {
                    Text("${item.progress} of ${item.pathSize}")
                }
            }
        }
    }
}