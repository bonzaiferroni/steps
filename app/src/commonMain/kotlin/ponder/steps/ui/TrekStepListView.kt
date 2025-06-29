package ponder.steps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepOutcome
import pondui.ui.behavior.MagicItem
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.LazyColumn

@Composable
fun TrekStepListView(
    viewModel: TrekStepListModel,
    pathId: StepId?,
//    branchStep: ((PathStepId?) -> Unit)?,
) {
    val state by viewModel.state.collectAsState()

    AddStepCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        createIntent = pathId == null,
        pathId = pathId,
        dismiss = viewModel::toggleAddItem
    )

    LazyColumn(1) {
        items(state.trekSteps, key = { it.pathStepId ?: it.trekId ?: it.stepId }) { trekStep ->
            val log = state.getLog(trekStep)
            val questions = if (log?.outcome == StepOutcome.Completed)
                state.questions[trekStep.stepId] ?: emptyList() else emptyList()
            val answers = log?.let { state.getAnswers(it.id) } ?: emptyList()
            val question = questions.firstOrNull { q -> answers.all { a -> a.questionId != q.id } }
            val canDragLeft = trekStep.trekId != null && trekStep.pathSize > 0 && question == null

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
                    trekStep = trekStep,
                    isFinished = trekStep.finishedAt != null || log != null,
                    isDeeper = true,
                    setOutcome = viewModel::setOutcome,
                    questionCount = questions.size,
                    loadTrek = viewModel::loadDeeperTrek,
//                    branchStep = branchStep
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