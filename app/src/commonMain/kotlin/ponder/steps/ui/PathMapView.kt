package ponder.steps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ponder.steps.PathEditorRoute
import ponder.steps.db.TrekPointId
import ponder.steps.model.data.Step
import pondui.ui.behavior.magic
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.LocalAppWindow
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Text
import pondui.ui.controls.WindowSizeClass
import pondui.ui.controls.bottomBarSpacerItem
import pondui.ui.nav.LocalNav
import pondui.ui.theme.Pond
import pondui.utils.addShadow
import kotlin.text.get

@Composable
fun PathMapView(
    viewModel: PathContextModel,
    navToPath: (Step) -> Unit,
) {
    val state by viewModel.stateFlow.collectAsState()
    val appWindow = LocalAppWindow.current
    val nav = LocalNav.current

    val pathStep = state.step ?: return

    LazyColumn(1, Alignment.CenterHorizontally) {

        item("header") {
            if (appWindow.widthSizeClass == WindowSizeClass.Compact) {
                Box(
                    modifier = Modifier.clip(Pond.ruler.defaultCorners)
                        .magic(offsetX = (-20).dp)
                ) {
                    // feature image
                    StepImage(
                        url = pathStep.imgUrl,
                        modifier = Modifier.fillMaxWidth()
                            .aspectRatio(1f)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(.6f))
                    ) {
                        // label
                        Text(
                            text = pathStep.label,
                            style = Pond.typo.h1.addShadow(),
                            modifier = Modifier.padding(Pond.ruler.unitPadding)
                        )
                    }
                }
                // description
                state.step?.description?.let {
                    Text(
                        text = it,
                        style = Pond.typo.bodyLarge,
                        modifier = Modifier.padding(Pond.ruler.unitPadding)
                            .magic(offsetX = 20.dp)
                    )
                }
            } else {
                Row(1) {
                    // feature image
                    StepImage(
                        url = pathStep.imgUrl,
                        modifier = Modifier.weight(1f)
                            .clip(Pond.ruler.defaultCorners)
                            .aspectRatio(1f)
                            .magic(offsetX = (-20).dp)
                    )
                    Column(
                        spacingUnits = 1,
                        modifier = Modifier.weight(1f)
                            .magic(offsetX = 20.dp)
                    ) {
                        // label
                        Text(
                            text = pathStep.label,
                            style = Pond.typo.h1.addShadow(),
                            modifier = Modifier.padding(Pond.ruler.unitPadding)
                        )
                        // description
                        state.step?.description?.let {
                            Text(
                                it,
                                Pond.typo.bodyLarge,
                                modifier = Modifier.padding(Pond.ruler.unitPadding)
                            )
                        }
                    }
                }
            }
        }

        item("edit button") {
            Column(
                spacingUnits = 1,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                state.progressRatio?.let { progressRatio ->
                    ProgressBar(
                        progress = progressRatio,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${state.progress} of ${pathStep.pathSize}")
                    }
                }
                Row(1) {
                    Button("Edit Path") { nav.go(PathEditorRoute(pathStep.id)) }
                }
            }
        }

        items(state.steps, key = { it.pathStepId ?: it.id }) { step ->
            val log = state.getLog(step)
            val progress = state.getProgress(step)
            val questions = state.questions[step.id] ?: emptyList()
            val answers = log?.let { state.getAnswers(it.id) } ?: emptyList()
            val question = log?.let { questions.firstOrNull { q -> answers.all { a -> a.questionId != q.id } } }

            PathMapStep(
                step = step,
                isSelected = state.selectedStepId == step.id,
                isLastStep = (step.position ?: 0) == pathStep.pathSize - 1,
                isTrekContext = state.isTrekContext,
                log = log,
                progress = progress,
                questionsAndAnswers = QuestionsAndAnswers(questions, answers),
                currentQuestion = question,
                setOutcome = viewModel::setOutcome,
                onFocusChanged = { focusState ->
                    when (focusState.isFocused) {
                        true -> viewModel.setFocus(step.id)
                        false -> viewModel.setFocus(null)

                    }
                },
                answerQuestion = { answerText ->
                    if (log != null && question != null)
                        viewModel.answerQuestion(step, log, question, answerText)
                },
                navToPath = navToPath,
            )
        }

        bottomBarSpacerItem()
    }
}