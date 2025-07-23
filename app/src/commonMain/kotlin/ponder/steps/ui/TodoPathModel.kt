package ponder.steps.ui

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel
import pondui.ui.core.ViewState

class TodoPathModel(
    private val trekPath: TrekPath,
    navToTrekPath: (TrekPath?, Boolean) -> Unit,
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
    private val stepRepo: LocalStepRepository = LocalStepRepository(),
    private val stepLogRepo: LocalStepLogRepository = LocalStepLogRepository(),
    private val questionsRepo: QuestionSource = QuestionSource(),
    private val answersRepo: LocalAnswerRepository = LocalAnswerRepository(),
) : StateModel<TodoPathState>() {

    override val state = ViewState(TodoPathState())

    val todoList = TodoListModel(
        viewModel = this,
        trekPath = trekPath,
        navToTrekPath = navToTrekPath,
    )

    private var stepFlowJob: Job? = null

    fun activate() {
        val pathId = trekPath.pathId;
        val trekPointId = trekPath.trekPointId
        stepFlowJob?.cancel()
        stepFlowJob = stepRepo.flowStep(pathId).launchCollect { step ->
            setState { it.copy(step = step) }
        }

        todoList.clearJobs()
        todoList.setFlows(
            stepFlow = trekRepo.flowPathTodoSteps(trekPath.trekPointId, pathId).map { todoSteps ->
                todoSteps.sortedBy { it.step.position }
            },
            stepLogFlow = stepLogRepo.flowPathLogsByTrekPointId(pathId, trekPointId),
            questionFlow = questionsRepo.flowPathQuestions(pathId),
            answerFlow = answersRepo.flowPathAnswersByTrekId(pathId, trekPointId),
            progressFlow = trekRepo.flowPathProgresses(pathId, trekPointId)
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