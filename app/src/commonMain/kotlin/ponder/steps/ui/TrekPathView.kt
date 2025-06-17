package ponder.steps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Plus
import ponder.steps.model.data.Question
import pondui.ui.behavior.MagicItem
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.Expando
import pondui.ui.controls.H3
import pondui.ui.controls.IconButton
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.TextButton
import pondui.ui.theme.Pond

@Composable
fun TrekPathView() {
    val viewModel = viewModel { TrekPathModel() }
    val state by viewModel.state.collectAsState()

    AddStepCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        createIntent = state.trek == null,
        pathId = state.trek?.stepId,
        dismiss = viewModel::toggleAddItem
    )

    LazyColumn(1) {
        item("controls") {
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                // MenuWheel(state.span, TrekSpan.entries.toImmutableList()) { viewModel::setSpan }
                Button(TablerIcons.Plus, onClick = viewModel::toggleAddItem)
            }
        }

        item("image") {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MagicItem(state.trek, offsetX = if (state.isDeeper) 30.dp else (-30).dp) { trekStep ->
                    StepImage(
                        url = trekStep?.imgUrl,
                        modifier = Modifier.clip(Pond.ruler.defaultCorners)
                            .width(200.dp)
                    )
                }
            }
        }

        state.trek?.let { trekStep ->
            item(trekStep.pathStepId ?: trekStep.trekId ?: trekStep.stepId) {
                Row(1, modifier = Modifier.animateItem()) {
                    IconButton(TablerIcons.ArrowLeft) { viewModel.loadTrek(trekStep.superId, false) }
                    H3(trekStep.stepLabel, modifier = Modifier.weight(1f))
                }
            }
        }
        items(state.steps, key = { it.pathStepId ?: it.trekId ?: it.stepId }) { trekStep ->
            val question: Question? = null

            MagicItem(
                item = question,
                offsetX = 50.dp,
                itemContent = { question ->
                    // QuestionRow(question) { viewModel.answerQuestion(item.trekId, question, it) }
                },
                isVisibleInit = true,
                modifier = Modifier.height(72.dp)
                    .animateItem()
            ) {
                TrekStepRow(
                    item = trekStep,
                    isFinished = false,
                    isHeader = false,
                    isDeeper = state.isDeeper,
                    completeStep = { step, outcome -> },
                    loadTrek = { viewModel.loadTrek(it, true) },
                    branchStep = viewModel::branchStep
                )
            }

//            Row(1, modifier = Modifier.animateItem()) {
//                Text(trekStep.stepLabel)
//                Expando()
//                val trekId = trekStep.trekId

//            }
        }

        item(key = "bottom spacer") {
            BottomBarSpacer()
        }
    }
}