package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.steps.io.AnswerRepository
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.StepLogRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.Answer
import ponder.steps.model.data.Question
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekStep
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.days

class TrekPathModel(
    private val trekRepo: TrekRepository = LocalTrekRepository(),
    private val questionRepo: QuestionRepository = LocalQuestionRepository(),
    private val answerRepo: AnswerRepository = LocalAnswerRepository(),
    private val stepLogRepo: StepLogRepository = LocalStepLogRepository(),
): StateModel<TrekPathState>(TrekPathState()) {

    init {
        loadTrek(null, true)
    }

    fun loadTrek(trekId: String?, isDeeper: Boolean) {
        clearJobs()
        if (trekId != null) {
            trekRepo.flowTrekStepById(trekId).launchCollectJob { trekStep ->
                val steps = stateNow.steps.filter { it.pathStepId != trekStep.pathStepId }
                setState { it.copy(trek = trekStep, steps = steps) }
            }
            trekRepo.flowTrekStepsBySuperId(trekId).launchCollectJob { trekSteps ->
                setState { it.copy(steps = trekSteps.sortedBy { trek -> trek.position }) }
            }
            stepLogRepo.flowPathLogsByTrekId(trekId).launchCollectJob { logs ->
                setState { it.copy(logs = logs) }
            }
            questionRepo.flowPathQuestionsByTrekId(trekId).launchCollectJob { questions ->
                setState { it.copy(questions = questions ) }
            }
            answerRepo.flowPathQuestionsByTrekId(trekId).launchCollectJob { answers ->
                setState { it.copy(answers = answers) }
            }
        } else {
            val start = Clock.startOfDay()
            val end = start + 1.days
            trekRepo.flowRootTrekSteps(start, end).launchCollectJob { trekSteps ->
                setState { it.copy(steps = trekSteps.sortedBy { trek -> trek.availableAt }) }
            }
            setState { it.copy(trek = null) }
        }
        setState { it.copy(isDeeper = isDeeper) }
    }

    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun branchStep(pathStepId: String?) {
        val trekId = stateNow.trek?.trekId ?: return
        val pathStepId = pathStepId ?: return
        viewModelScope.launch {
            val id = trekRepo.createSubTrek(trekId, pathStepId)
            loadTrek(id, true)
        }
    }

    fun setOutcome(trekStep: TrekStep, outcome: StepOutcome? = null) {
        val trekId = trekStep.superId ?: trekStep.trekId ?: error("No trekId")
        viewModelScope.launch {
            trekRepo.setOutcome(trekId, trekStep.stepId, trekStep.pathStepId, outcome)
        }
    }

    fun answerQuestion(stepLog: StepLog, question: Question, answerText: String) {
        viewModelScope.launch {
            answerRepo.createAnswer(stepLog.id, question.id, answerText, question.type)
        }
    }
}

data class TrekPathState(
    val trek: TrekStep? = null,
    val steps: List<TrekStep> = emptyList(),
    val logs: List<StepLog> = emptyList(),
    val isAddingItem: Boolean = false,
    val isDeeper: Boolean = false,
    val stepLogs: List<StepLog> = emptyList(),
    val questions: Map<String, List<Question>> = emptyMap(), // String is pathStepId
    val answers: Map<String, List<Answer>> = emptyMap(), // String is pathStepId
)
