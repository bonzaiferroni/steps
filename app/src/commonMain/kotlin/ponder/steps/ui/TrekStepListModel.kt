package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import ponder.steps.db.StepId
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.Answer
import ponder.steps.model.data.NewAnswer
import ponder.steps.model.data.PathStepId
import ponder.steps.model.data.Question
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepLogId
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekId
import ponder.steps.model.data.TrekStep
import pondui.ui.core.SubModel

@Stable
class TrekStepListModel(
    viewModel: ViewModel,
    private val loadTrek: (TrekId?, Boolean) -> Unit,
    private val trekRepo: TrekRepository = LocalTrekRepository(),
    private val questionRepo: LocalQuestionRepository = LocalQuestionRepository(),
    private val answerRepo: LocalAnswerRepository = LocalAnswerRepository(),
    private val stepLogRepo: LocalStepLogRepository = LocalStepLogRepository(),
) : SubModel<TrekStepListState>(TrekStepListState(), viewModel) {

    var allTrekSteps: List<TrekStep> = emptyList()
    var trekStepFilter: ((TrekStep) -> Boolean)? = null

    fun setTrekSteps(trekSteps: List<TrekStep>) {
        clearJobs()
        val stepIds = trekSteps.map { it.stepId }
        val trekIds = trekSteps.mapNotNull { it.trekId ?: it.superId }
        questionRepo.flowQuestionsByStepIds(stepIds).launchCollect { questions ->
            setState { it.copy(questions = questions) }
        }
        stepLogRepo.flowLogsByTrekIds(trekIds).launchCollect { stepLogs ->
            setState { it.copy(stepLogs = stepLogs) }
        }
        answerRepo.flowAnswersByTrekIds(trekIds).launchCollect { answers ->
            setState { it.copy(answers = answers) }
        }
        allTrekSteps = trekSteps
        refreshSteps()
    }

    private fun refreshSteps() {
        val filteredSteps = trekStepFilter?.let { filter -> allTrekSteps.filter { filter(it) } } ?: allTrekSteps
        setState { it.copy(trekSteps = filteredSteps) }
    }

    fun setFilter(filter: ((TrekStep) -> Boolean)?) {
        trekStepFilter = filter
        refreshSteps()
    }

    fun setOutcome(trekStep: TrekStep, outcome: StepOutcome? = null) {
        val trekId = trekStep.superId ?: trekStep.trekId ?: error("No trekId")
        ioLaunch {
            trekRepo.setOutcome(trekId, trekStep.stepId, trekStep.pathStepId, outcome)
        }
    }

    fun answerQuestion(trekStep: TrekStep, stepLog: StepLog, question: Question, answerText: String) {
        val trekId = trekStep.superId ?: trekStep.trekId ?: error("No trekId")
        ioLaunch {
            trekRepo.createAnswer(trekId, NewAnswer(stepLog.id, question.id, answerText, question.type))
        }
    }

    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun loadDeeperTrek(trekId: TrekId?, superId: TrekId?, pathStepId: PathStepId?) {
        if (trekId != null) {
            loadTrek(trekId, true)
        } else if (superId != null && pathStepId != null) {
            ioLaunch {
                val id = trekRepo.createSubTrek(superId, pathStepId)
                loadTrek(id, true)
            }
        }
    }
}

data class TrekStepListState(
    val trekSteps: List<TrekStep> = emptyList(),
    val stepLogs: List<StepLog> = emptyList(),
    val questions: Map<StepId, List<Question>> = emptyMap(),
    val answers: Map<StepLogId, List<Answer>> = emptyMap(),
    val isAddingItem: Boolean = false,
) {
    fun getLog(trekStep: TrekStep) = stepLogs.firstOrNull {
        if (it.pathStepId != null) it.pathStepId == trekStep.pathStepId else it.trekId == trekStep.trekId
    }
    fun getAnswers(stepLogId: StepLogId) = answers[stepLogId] ?: emptyList()

    val totalProgress get() = trekSteps.count { it.finishedAt != null }
    val totalSteps get() = trekSteps.size
    val progressRatio get() = totalProgress / (totalSteps.takeIf { it > 0 } ?: 1).toFloat()
}