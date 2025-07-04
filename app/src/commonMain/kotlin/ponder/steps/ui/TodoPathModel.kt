package ponder.steps.ui

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import ponder.steps.db.TodoStep
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepId
import ponder.steps.model.data.TrekId
import pondui.ui.core.StateModel

class TodoPathModel(
    private val trekPath: TrekPath,
    navToTrekPath: (TrekPath?, Boolean) -> Unit,
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
    private val stepRepo: LocalStepRepository = LocalStepRepository(),
    private val stepLogRepo: LocalStepLogRepository = LocalStepLogRepository(),
    private val questionsRepo: LocalQuestionRepository = LocalQuestionRepository(),
    private val answersRepo: LocalAnswerRepository = LocalAnswerRepository(),
) : StateModel<TodoPathState>(TodoPathState()) {

    val todoList = TodoListModel(
        viewModel = this,
        trekPath = trekPath,
        navToTrekPath = navToTrekPath,
    )

    private var stepFlowJob: Job? = null

    fun activate() {
        val pathId = trekPath.pathId;
        val trekId = trekPath.trekId
        stepFlowJob?.cancel()
        stepFlowJob = stepRepo.flowStep(pathId).launchCollect { step ->
            setState { it.copy(step = step) }
        }

        todoList.clearJobs()
        todoList.setFlows(
            stepFlow = stepRepo.flowPathSteps(pathId).map { steps ->
                steps.map { step -> TodoStep(trekId, step) }
                    .sortedBy { it.step.position }
            },
            stepLogFlow = stepLogRepo.flowPathLogsByTrekId(pathId, trekId),
            questionFlow = questionsRepo.flowPathQuestions(pathId),
            answerFlow = answersRepo.flowPathAnswersByTrekId(pathId, trekId),
            progressFlow = trekRepo.flowPathProgresses(pathId, trekId)
        )
    }

    fun deactivate() {
        stepFlowJob?.cancel()
        todoList.clearJobs()
    }
}

data class TodoPathState(
    val step: Step? = null,
)