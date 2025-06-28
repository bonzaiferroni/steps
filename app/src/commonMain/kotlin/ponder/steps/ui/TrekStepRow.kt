package ponder.steps.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Plus
import kabinet.utils.startOfDay
import kotlinx.datetime.Clock
import ponder.steps.StepProfileRoute
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekStep
import pondui.ui.behavior.MagicItem
import pondui.ui.behavior.magic
import pondui.ui.controls.Checkbox
import pondui.ui.controls.Column
import pondui.ui.controls.ContentButton
import pondui.ui.controls.Icon
import pondui.ui.controls.IconButton
import pondui.ui.controls.Label
import pondui.ui.controls.ProgressBarButton
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond

@Composable
fun LazyItemScope.TrekStepRow(
    trekStep: TrekStep,
    isFinished: Boolean,
    isDeeper: Boolean,
    questionCount: Int,
    setOutcome: (TrekStep, StepOutcome?) -> Unit,
    loadTrek: (String) -> Unit,
//    branchStep: (String) -> Unit,
) {
    val nav = LocalNav.current
    val progress = trekStep.progress
    val trekId = trekStep.trekId
    val isTrek = trekId != null && progress != null && trekStep.pathSize > 0
    val isFinishedAnimated by animateFloatAsState(if (isFinished) 1f else 0f)

    Row(
        spacingUnits = 1,
        modifier = Modifier.fillMaxWidth()
            .animateItem()
            .magic(offsetX = if (isDeeper) 30.dp else (-30).dp)
    ) {
        Row(
            spacingUnits = 1,
            modifier = Modifier.graphicsLayer { alpha = (1f - isFinishedAnimated) * .5f + .5f }
        ) {
            ContentButton(
                onClick = { nav.go(StepProfileRoute(trekStep.stepId)) },
                shape = CircleShape
            ) {
                MagicItem(trekStep.thumbUrl, rotationX = 90) { url ->
                    StepImage(
                        url = url,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
            Row(1, modifier = Modifier.weight(1f)) {
                trekStep.position?.let { Label("${it + 1}.", Pond.typo.bodyLarge) }
                Column(0) {
                    Text(
                        text = trekStep.stepLabel,
                        style = Pond.typo.bodyLarge,
                        maxLines = 2,
                    )
                    if (!isTrek && trekStep.pathSize > 0) {
                        Label("${trekStep.pathSize} steps")
                    }
                    if (questionCount > 0) {
                        Label("$questionCount questions")
                    }
                    val intentLabel = trekStep.intentLabel
                    if (intentLabel != null && intentLabel != trekStep.stepLabel) {
                        Row(1) {
                            Label("Path:")
                            Text(intentLabel)
                        }
                    }
                    val startOfDay = Clock.startOfDay()
                    val availableAt = trekStep.availableAt
                    if (availableAt != null && startOfDay > availableAt) {
                        Label("From ${(startOfDay - availableAt).inWholeDays + 1} days ago")
                    }
                }
            }
//            val pathStepId = trekStep.pathStepId
//            if (trekStep.trekId == null && pathStepId != null) {
//                IconButton(TablerIcons.Plus) { branchStep(pathStepId) }
//            }

            if (isTrek) {
                val progressRatio = progress / trekStep.pathSize.toFloat()
                ProgressBarButton(
                    progress = progressRatio,
                    onClick = { loadTrek(trekId) }
                ) {
                    Row(1) {
                        Text("${trekStep.progress} of ${trekStep.pathSize}")
                        Icon(TablerIcons.ArrowRight)
                    }
                }
            } else {
                Checkbox(isFinished) {
                    setOutcome(trekStep, if (isFinished) null else StepOutcome.Completed)
                }
            }
        }
    }
}