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
import ponder.steps.StepProfileRoute
import ponder.steps.db.TodoStep
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekId
import pondui.ui.behavior.MagicItem
import pondui.ui.behavior.magic
import pondui.ui.controls.Checkbox
import pondui.ui.controls.Column
import pondui.ui.controls.ContentButton
import pondui.ui.controls.Icon
import pondui.ui.controls.Label
import pondui.ui.controls.ProgressBarButton
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond

@Composable
fun LazyItemScope.TodoStepRow(
    todoStep: TodoStep,
    isFinished: Boolean,
    isDeeper: Boolean,
    questionCount: Int,
    progress: Int,
    pathSize: Int,
    setOutcome: (TrekId, Step, StepOutcome?) -> Unit,
    navToPath: (TrekId, Step) -> Unit,
//    branchStep: (String) -> Unit,
) {
    val trekId = todoStep.trekId; val step = todoStep.step
    val nav = LocalNav.current
    val isFinishedAnimated by animateFloatAsState(if (isFinished) 1f else 0f)

    Row(
        spacingUnits = 1,
        modifier = Modifier.fillMaxWidth()
            .animateItem()
    ) {
        Row(
            spacingUnits = 1,
            modifier = Modifier.graphicsLayer { alpha = (1f - isFinishedAnimated) * .5f + .5f }
        ) {
            ContentButton(
                onClick = { nav.go(StepProfileRoute(step.id)) },
                shape = CircleShape
            ) {
                MagicItem(step.thumbUrl, rotationX = 90) { url ->
                    StepImage(
                        url = url,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
            Row(1, modifier = Modifier.weight(1f)) {
                step.position?.let { Label("${it + 1}.", Pond.typo.bodyLarge) }
                Column(0) {
                    Text(
                        text = step.label,
                        style = Pond.typo.bodyLarge,
                        maxLines = 2,
                    )
                    if (questionCount > 0) {
                        Label("$questionCount questions")
                    }
//                    val startOfDay = Clock.startOfDay()
//                    val availableAt = availableAt
//                    if (availableAt != null && startOfDay > availableAt) {
//                        Label("From ${(startOfDay - availableAt).inWholeDays + 1} days ago")
//                    }
                }
            }

            if (pathSize > 0) {
                val progressRatio = progress / pathSize.toFloat()
                ProgressBarButton(
                    progress = progressRatio,
                    onClick = { navToPath(trekId, step) }
                ) {
                    Row(1) {
                        Text("$progress of ${pathSize}")
                        Icon(TablerIcons.ArrowRight)
                    }
                }
            } else {
                Checkbox(isFinished) {
                    setOutcome(trekId, step, if (isFinished) null else StepOutcome.Completed)
                }
            }
        }
    }
}