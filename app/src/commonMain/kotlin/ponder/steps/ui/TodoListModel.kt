package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import ponder.steps.db.StepId
import ponder.steps.db.TodoStep
import ponder.steps.db.TrekPointId
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.StepOutcome
import ponder.steps.model.data.Answer
import ponder.steps.model.data.NewAnswer
import ponder.steps.model.data.Question
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepLogId
import ponder.steps.model.data.StepStatus
import ponder.steps.model.data.TrekId
import pondui.ui.core.SubModel

@Stable
class TodoListModel(
    private val viewModel: ViewModel,
    private val trekPath: TrekPath?,
    private val navToTrekPath: (TrekPath?, Boolean) -> Unit,
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
) : SubModel<TodoListState>(TodoListState(trekPath == null), viewModel) {

    private var allSteps: List<TodoStep> = emptyList()
    var stepFilter: ((TodoStep) -> Boolean)? = null

    fun setFlows(
        stepFlow: Flow<List<TodoStep>>,
        stepLogFlow: Flow<List<StepLog>>,
        questionFlow: Flow<Map<StepId, List<Question>>>,
        answerFlow: Flow<Map<StepLogId, List<Answer>>>,
        progressFlow: Flow<Map<String, Int>>,
    ) {
        stepFlow.launchCollect { steps ->
            allSteps = steps
            refreshSteps()
        }
        stepLogFlow.launchCollect { stepLogs -> setState { it.copy(stepLogs = stepLogs) } }
        questionFlow.launchCollect { questions -> setState { it.copy(questions = questions) } }
        answerFlow.launchCollect { answers -> setState { it.copy(answers = answers) } }
        progressFlow.launchCollect { progresses -> setState { it.copy(progresses = progresses) } }
    }

    private fun refreshSteps() {
        val filteredSteps = stepFilter?.let { filter -> allSteps.filter { filter(it) } } ?: allSteps
        setState { it.copy(todoSteps = filteredSteps) }
    }

    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun setOutcome(trekPointId: TrekPointId, step: Step, outcome: StepOutcome? = null) {
        ioLaunch {
            trekRepo.setFinished(trekPointId, step, outcome, trekPath?.breadcrumbs)
        }
    }

    fun answerQuestion(trekId: TrekId, step: Step, stepLog: StepLog, question: Question, answerText: String?) {
        ioLaunch {
            trekRepo.createAnswer(
                trekId = trekId,
                step = step,
                answer = NewAnswer(stepLog.id, question.id, answerText, question.type),
                breadcrumbs = trekPath?.breadcrumbs
            )
        }
    }

    fun setFilter(filter: ((TodoStep) -> Boolean)?) {
        stepFilter = filter
        refreshSteps()
    }

    fun navToDeeperPath(trekPointId: TrekPointId, step: Step) {
        val trekPath = this.trekPath?.let { it.copy(pathId = step.id, breadcrumbs = it.breadcrumbs + step) }
            ?: TrekPath(trekPointId, pathId = step.id, breadcrumbs = listOf(step))
        navToTrekPath(trekPath, true)
    }
}

@Stable
data class TodoListState(
    val isRoot: Boolean,
    val todoSteps: List<TodoStep> = emptyList(),
    val stepLogs: List<StepLog> = emptyList(),
    val questions: Map<StepId, List<Question>> = emptyMap(),
    val answers: Map<StepLogId, List<Answer>> = emptyMap(),
    val progresses: Map<String, Int> = emptyMap(),
    val isAddingItem: Boolean = false,
) {
    fun getLog(todoStep: TodoStep) = when (isRoot) {
        true -> todoStep.trekId?.let { trekId -> stepLogs.firstOrNull { it.trekId == trekId  } }
        false -> stepLogs.firstOrNull { it.pathStepId == todoStep.step.pathStepId }
    }

    fun getAnswers(stepLogId: StepLogId) = answers[stepLogId] ?: emptyList()

    val progress get() = stepLogs.size
    val totalSteps get() = todoSteps.size
    val progressRatio get() = progress / (totalSteps.takeIf { it > 0 } ?: 1).toFloat()
}