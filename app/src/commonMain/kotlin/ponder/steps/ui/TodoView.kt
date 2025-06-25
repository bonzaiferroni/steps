package ponder.steps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Plus
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekStep
import ponder.steps.ui.TrekStepRow
import pondui.ui.behavior.MagicItem
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.H3
import pondui.ui.controls.IconButton
import pondui.ui.controls.Label
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.theme.Pond

@Composable
fun TodoView() {
    val viewModel = viewModel { TodoModel() }
    val state by viewModel.state.collectAsState()

    AddStepCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        createIntent = state.trek == null,
        pathId = state.trek?.stepId,
        dismiss = viewModel::toggleAddItem
    )

    val getKey: (TrekStep) -> Any = { it.pathStepId ?: it.trekId ?: it.stepId }

    LazyColumn(1) {

        item("image") {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MagicItem(
                    item = state.trek,
                    key = { state.trek?.trekId} ,
                    offsetX = if (state.isDeeper) 30.dp else (-30).dp
                ) { trekStep ->
                    StepImage(
                        url = trekStep?.imgUrl,
                        modifier = Modifier.clip(Pond.ruler.defaultCorners)
                            .width(200.dp)
                    )
                }
            }
        }

        state.trek?.let { trekStep ->
            item(getKey(trekStep)) {
                Row(1, modifier = Modifier.animateItem()) {
                    IconButton(TablerIcons.ArrowLeft) { viewModel.loadTrek(trekStep.superId, false) }
                    H3(trekStep.stepLabel, modifier = Modifier.weight(1f))
                }
            }
        }

        items(state.steps, key = getKey) { trekStep ->
            val log = state.getLog(trekStep)
            val questions = if (log?.outcome == StepOutcome.Completed)
                state.questions[trekStep.stepId] ?: emptyList() else emptyList()
            val answers = state.getAnswers(trekStep)
            val question = questions.firstOrNull { q -> answers.all { a -> a.questionId != q.id } }

            MagicItem(
                item = question,
                offsetX = 50.dp,
                itemContent = { question ->
                    QuestionRow(trekStep.stepLabel, question) { answerText ->
                        if (log != null && answerText != null)
                            viewModel.answerQuestion(trekStep, log, question, answerText)
                    }
                },
                isVisibleInit = true,
                modifier = Modifier.height(72.dp)
                    .animateItem()
            ) {
                TrekStepRow(
                    item = trekStep,
                    isFinished = trekStep.finishedAt != null || log != null,
                    isDeeper = state.isDeeper,
                    setOutcome = viewModel::setOutcome,
                    questionCount = questions.size,
                    loadTrek = { viewModel.loadTrek(it, true) },
                    branchStep = viewModel::branchStep
                )
            }
        }

        item("controls") {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Label("Completed", modifier = Modifier.weight(1f))
                Button(TablerIcons.Plus, onClick = viewModel::toggleAddItem)
            }
        }

        item(key = "bottom spacer") {
            BottomBarSpacer()
        }
    }
}