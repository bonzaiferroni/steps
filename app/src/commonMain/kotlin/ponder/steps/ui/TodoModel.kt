package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.steps.db.StepId
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
import ponder.steps.model.data.TrekId
import ponder.steps.model.data.TrekStep
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class TodoModel(
    private val trekRepo: TrekRepository = LocalTrekRepository(),
    private val questionRepo: QuestionRepository = LocalQuestionRepository(),
    private val answerRepo: AnswerRepository = LocalAnswerRepository(),
    private val stepLogRepo: StepLogRepository = LocalStepLogRepository(),
): StateModel<TodoState>(TodoState()) {

    init {
        loadTrek(null, true)
        viewModelScope.launch {
            while (true) {
                trekRepo.syncTreksWithIntents()
                delay(1.minutes)
            }
        }
    }

    fun loadTrek(trekId: String?, isDeeper: Boolean) {
        clearJobs()
        if (trekId != null) {
            trekRepo.flowTrekStepById(trekId).launchCollect { trekStep ->
                val steps = stateNow.steps.filter { it.pathStepId != trekStep.pathStepId }
                setState { it.copy(trek = trekStep, steps = steps) }
            }
            trekRepo.flowTrekStepsBySuperId(trekId).launchCollect { trekSteps ->
                setState { it.copy(steps = trekSteps.sortedBy { trek -> trek.position }) }
            }
            stepLogRepo.flowPathLogsByTrekId(trekId).launchCollect(::setLogs)
            questionRepo.flowPathQuestionsByTrekId(trekId).launchCollect(::setQuestions)
            answerRepo.flowPathQuestionsByTrekId(trekId).launchCollect(::setAnswers)
        } else {
            val start = Clock.startOfDay()
            val end = start + 1.days
            trekRepo.flowRootTrekSteps(start, end).launchCollect { trekSteps ->
                setState { it.copy(steps = trekSteps.sortedBy { trek -> trek.availableAt }, trek = null) }
            }
            stepLogRepo.flowRootLogs(start, end).launchCollect(::setLogs)
            questionRepo.flowRootQuestions(start, end).launchCollect(::setQuestions)
            answerRepo.flowRootAnswers(start, end).launchCollect(::setAnswers)
        }
        setState { it.copy(isDeeper = isDeeper) }
    }

    private fun setLogs(logs: List<StepLog>) = setState { it.copy(logs = logs) }
    private fun setQuestions(questions: Map<String, List<Question>>) = setState { it.copy(questions = questions ) }
    private fun setAnswers(answers: Map<String, List<Answer>>) = setState { it.copy(answers = answers) }

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

    fun answerQuestion(trekStep: TrekStep, stepLog: StepLog, question: Question, answerText: String) {
        val trekId = trekStep.superId ?: trekStep.trekId ?: error("No trekId")
        viewModelScope.launch {
            trekRepo.createAnswer(trekId, Answer(stepLog.id, question.id, answerText, question.type))
        }
    }
}

data class TodoState(
    val trek: TrekStep? = null,
    val steps: List<TrekStep> = emptyList(),
    val logs: List<StepLog> = emptyList(),
    val isAddingItem: Boolean = false,
    val isDeeper: Boolean = false,
    val stepLogs: List<StepLog> = emptyList(),
    val questions: Map<StepId, List<Question>> = emptyMap(),
    val answers: Map<TrekIdOrPathStepId, List<Answer>> = emptyMap(),
) {
    fun getLog(trekStep: TrekStep) = logs.firstOrNull {
        if (it.pathStepId != null) it.pathStepId == trekStep.pathStepId else it.trekId == trekStep.trekId
    }
    fun getAnswers(trek: TrekStep) = answers[(trek.pathStepId ?: trek.trekId)] ?: emptyList()
}

typealias TrekIdOrPathStepId = String