package ponder.steps.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Plus
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekStep
import pondui.ui.behavior.MagicItem
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.H1
import pondui.ui.controls.H3
import pondui.ui.controls.Icon
import pondui.ui.controls.IconButton
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.ProgressBarButton
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.theme.Pond

@Composable
fun TodoView() {
    val viewModel = viewModel { TodoModel() }
    val state by viewModel.state.collectAsState()

    var offsetX by remember { mutableStateOf(0f) }
    var draggedItem by remember (state.trek?.trekId) { mutableStateOf<TrekStep?>(null) }
    val dragAnimator = remember { Animatable(0f) }
    val dragAnimation by dragAnimator.asState()
    var animatedItem by remember { mutableStateOf<TrekStep?>(null) }
    val canDragRight = state.trek != null

    LaunchedEffect(offsetX, draggedItem) {
        if (offsetX != 0f && draggedItem == null) {
            dragAnimator.animateTo(0f)
            animatedItem = null
            offsetX = 0f
        } else {
            animatedItem = draggedItem
            dragAnimator.snapTo(offsetX)
        }
    }
    LaunchedEffect(offsetX) {
        val item = draggedItem
        if (offsetX < -100 && item != null) {
            viewModel.loadTrek(item.trekId, true)
        }
        if (offsetX > 100) {
            viewModel.loadTrek(state.trek?.superId, false)
        }
    }

    AddStepCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        createIntent = state.trek == null,
        pathId = state.trek?.stepId,
        dismiss = viewModel::toggleAddItem
    )

    LazyColumn(1) {

        item("header") {
            Section {
                Column(1, horizontalAlignment = Alignment.CenterHorizontally) {
                    val trekStep = state.trek
                    Row(
                        spacingUnits = 1,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(50.dp)) {
                            if (trekStep != null) {
                                IconButton(TablerIcons.ArrowLeft) { viewModel.loadTrek(trekStep.superId, false)  }
                            }
                        }
                        MagicItem(
                            item = state.trek,
                            key = { state.trek?.trekId},
                            offsetX = if (state.isDeeper) 30.dp else (-30).dp,
                            modifier = Modifier.weight(1f)
                        ) { trekStep ->
                            StepImage(
                                url = trekStep?.imgUrl,
                                modifier = Modifier.clip(Pond.ruler.defaultCorners)
                                    .width(200.dp)
                            )
                        }
                        Box(modifier = Modifier.width(50.dp))
                    }

                    H1(trekStep?.stepLabel ?: "Today's Journey")
                    ProgressBar(
                        progress = state.progressRatio,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("${state.totalProgress} of ${state.totalSteps}")
                    }
                }
            }
        }

        items(state.steps, key = { it.pathStepId ?: it.trekId ?: it.stepId }) { trekStep ->
            val log = state.getLog(trekStep)
            val questions = if (log?.outcome == StepOutcome.Completed)
                state.questions[trekStep.stepId] ?: emptyList() else emptyList()
            val answers = state.getAnswers(trekStep)
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
                    .graphicsLayer {
                        if (dragAnimation < 0) {
                            if (trekStep == animatedItem)
                                translationX = dragAnimation
                            else
                                alpha = (100 + dragAnimation) / 100
                        } else {
                            translationX = dragAnimation
                            alpha = (100 - dragAnimation) / 100
                        }
                    }
//                    .pointerInput(Unit) {
//                        detectDragGestures(
//                            onDragStart = { offset ->
//                            },
//                            onDrag = { change, dragAmount ->
//                                if (dragAmount.x < 0 && canDragLeft || dragAmount.x > 0 && canDragRight) {
//                                    change.consume()
//                                    draggedItem = trekStep
//                                    offsetX += dragAmount.x
//                                }
//                            },
//                            onDragEnd = {
//                                draggedItem = null
//                            }
//                        )
//                    }
            ) {
                TrekStepRow(
                    trekStep = trekStep,
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