package ponder.steps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import kotlinx.collections.immutable.ImmutableList
import ponder.steps.model.data.*
import pondui.ui.behavior.selected
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.H1
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.LazyRow
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.theme.Pond
import kotlin.collections.get

@Composable
fun TodoRootView(
    navToPath: (TrekPath?, Boolean) -> Unit,
) {
    val viewModel = viewModel { TodoRootModel(navToPath) }
    val state by viewModel.stateFlow.collectAsState()

    AddStepCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        createIntent = true,
        pathId = null,
        dismiss = viewModel::toggleAddItem
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(1) {
            Section {
                Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
                    H1("Today's Journey")
                    ProgressBar(
                        progress = state.progressRatio,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${state.progress} of ${state.totalSteps}")
                    }
                }
            }

            TagSetRow(state.selectedTag, state.tagSet, viewModel::clickTag)

            LazyColumn(1) {
                items(state.todoSteps, key = { it.vmKey }) { todoStep ->
                    val step = todoStep.step
                    val log = state.getLog(todoStep)
                    val progress = state.progresses[todoStep.progressKey]
                    val questions = state.questions[step.id] ?: emptyList()
                    val answers = log?.let { state.getAnswers(it.id) } ?: emptyList()
                    val question = log?.let {
                        if (log.status != StepStatus.Skipped) {
                            questions.firstOrNull { q -> answers.none { a -> a.questionId == q.id } }
                        } else null
                    }

                    PathMapStep(
                        step = step,
                        isSelected = false,
                        isLastStep = true,
                        isCompleted = log != null,
                        progress = progress,
                        questionsAndAnswers = QuestionsAndAnswers(questions, answers),
                        currentQuestion = question,
                        setOutcome = { step, stepOutcome ->
                            viewModel.setOutcome(todoStep.trekPointId, step, stepOutcome)
                        },
                        onFocusChanged = { },
                        answerQuestion = { answerText ->
                            if (log != null && question != null)
                                viewModel.answerQuestion(todoStep.trekPointId, step, log, question, answerText)
                        },
                        navToPath = { step ->
                            navToPath(TrekPath(todoStep.trekPointId, step.id, listOf(step)), false)
                        }
                    )
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
    }
}

@Composable
fun TagSetRow(
    selectedTag: Tag?,
    tagSet: ImmutableList<Tag>,
    clickTag: (Tag) -> Unit
) {
    LazyRow(1) {
        items(tagSet) { tag ->
            Button(tag.label, Pond.colors.action, modifier = Modifier.selected(tag == selectedTag)) { clickTag(tag) }
        }
    }
}