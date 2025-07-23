package ponder.steps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepStatus
import pondui.ui.behavior.MagicItem
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.LazyColumn

@Composable
fun TodoListView(
    viewModel: TodoListModel,
    pathId: StepId?,
//    branchStep: ((PathStepId?) -> Unit)?,
) {
    val state by viewModel.stateFlow.collectAsState()

    AddStepCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        createIntent = pathId == null,
        pathId = pathId,
        dismiss = viewModel::toggleAddItem
    )

    LazyColumn(1) {
        items(state.todoSteps, key = { it.vmKey }) { todoStep ->
            val step = todoStep.step
            val log = state.getLog(todoStep)
            val progress = state.progresses[todoStep.progressKey] ?: 0
            val questions = if (log?.status == StepStatus.AskedQuestion)
                state.questions[step.id] ?: emptyList() else emptyList()
            val answers = log?.let { state.getAnswers(it.id) } ?: emptyList()
            val question = questions.firstOrNull { q -> answers.all { a -> a.questionId != q.id } }

            MagicItem(
                item = question,
                offsetX = 50.dp,
                itemContent = { question ->
                    QuestionRow(step.label, question) { answerText ->
                        val trekId = todoStep.trekId
                        if (trekId != null && log != null)
                            viewModel.answerQuestion(trekId, step, log, question, answerText)
                    }
                },
                isVisibleInit = true,
                modifier = Modifier.animateItem()
            ) {
                TodoStepRow(
                    todoStep = todoStep,
                    isCompleted = log?.status == StepStatus.Completed,
                    questionCount = questions.size,
                    progress = progress,
                    pathSize = step.pathSize,
                    setOutcome = viewModel::setOutcome,
                    navToPath = viewModel::navToDeeperPath,
                )
            }
        }

        item("controls") {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth().animateItem()
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