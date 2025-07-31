package ponder.steps.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kabinet.utils.pluralize
import ponder.steps.PathEditorRoute
import ponder.steps.db.TrekPointId
import ponder.steps.model.data.MaterialType
import ponder.steps.model.data.Step
import pondui.ui.behavior.AlignX
import pondui.ui.behavior.drawLabel
import pondui.ui.behavior.drawSection
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

    val pathStep = state.step ?: return

    val materials = state.stepMaterials

    LazyColumn(1, Alignment.CenterHorizontally) {

        item("header") {
            PathMapHeader(viewModel)
        }

        if (materials.isNotEmpty()) {
            item("materials step") {
                val tools = remember(materials) { materials.filter { it.materialType == MaterialType.Tool } }
                val ingredients =
                    remember(materials) { materials.filter { it.materialType == MaterialType.Ingredient } }

                PathMapItemPart(
                    verticalAlignment = Alignment.Top,
                    lineSlot = {
                        Column {
                            StepLineCircle() {
                                StepImage(
                                    url = null,
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(StepLineStrokeWidth * 2)
                                        .drawLabel("step 0", alignX = AlignX.Center)
                                        .clip(CircleShape)
                                )
                            }
                            StepLineFill(true)
                            StepLineTail(true)
                        }
                    }
                ) {
                    Column(1) {
                        if (tools.isNotEmpty()) {
                            Column(
                                spacingUnits = 1,
                                modifier = Modifier.drawSection("${tools.size} tool${pluralize(tools.size)}")
                                    .fillMaxWidth()
                                    .animateContentSize()
                            ) {
                                for (stepMaterial in tools) {
                                    Text(stepMaterial.label)
                                    // MaterialRow(stepMaterial, viewModel)
                                }
                            }
                        }
                        if (ingredients.isNotEmpty()) {
                            Column(
                                spacingUnits = 1,
                                modifier = Modifier.drawSection("${ingredients.size} ingredient${pluralize(ingredients.size)}")
                                    .fillMaxWidth()
                                    .animateContentSize()
                            ) {
                                for (stepMaterial in ingredients) {
                                    Text(stepMaterial.label)
                                    // MaterialRow(stepMaterial, viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }

        items(state.steps, key = { it.pathStepId ?: it.id }) { step ->
            val log = state.getLog(step)
            val progress = state.getProgress(step)
            val questions = state.questions[step.id] ?: emptyList()
            val answers = log?.let { state.getAnswers(it.id) }
            val question = if (answers != null) {
                questions.firstOrNull { q -> answers.all { a -> a.questionId != q.id } }
            } else null

            PathMapStep(
                step = step,
                isSelected = state.selectedStepId == step.id,
                isLastStep = (step.position ?: 0) == pathStep.pathSize - 1,
                isCompleted = if (state.isTrekContext) log != null else null,
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