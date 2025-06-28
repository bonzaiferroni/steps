package ponder.steps.ui

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ponder.steps.db.StepId
import ponder.steps.io.AnswerRepository
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.io.StepLogRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.Answer
import ponder.steps.model.data.NewAnswer
import ponder.steps.model.data.PathStepId
import ponder.steps.model.data.Question
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekId
import ponder.steps.model.data.TrekStep
import pondui.ui.core.SubModel

@Stable
abstract class TrekStepListModel(
    private val loadTrek: (TrekId?, Boolean) -> Unit,
    override val coroutineScope: CoroutineScope,
    protected val trekRepo: TrekRepository = LocalTrekRepository(),
    protected val questionRepo: QuestionRepository = LocalQuestionRepository(),
    protected val answerRepo: AnswerRepository = LocalAnswerRepository(),
    protected val stepLogRepo: StepLogRepository = LocalStepLogRepository(),
) : SubModel<TrekStepListState>(TrekStepListState()) {

    protected fun setLogs(logs: List<StepLog>) = setState { it.copy(logs = logs) }
    protected fun setQuestions(questions: Map<String, List<Question>>) = setState { it.copy(questions = questions ) }
    protected fun setAnswers(answers: Map<String, List<Answer>>) = setState { it.copy(answers = answers) }

    fun setOutcome(trekStep: TrekStep, outcome: StepOutcome? = null) {
        val trekId = trekStep.superId ?: trekStep.trekId ?: error("No trekId")
        coroutineScope.launch {
            trekRepo.setOutcome(trekId, trekStep.stepId, trekStep.pathStepId, outcome)
        }
    }

    fun answerQuestion(trekStep: TrekStep, stepLog: StepLog, question: Question, answerText: String) {
        val trekId = trekStep.superId ?: trekStep.trekId ?: error("No trekId")
        coroutineScope.launch {
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
            coroutineScope.launch {
                val id = trekRepo.createSubTrek(superId, pathStepId)
                loadTrek(id, true)
            }
        }
    }
}

data class TrekStepListState(
    val steps: List<TrekStep> = emptyList(),
    val logs: List<StepLog> = emptyList(),
    val questions: Map<StepId, List<Question>> = emptyMap(),
    val answers: Map<TrekIdOrPathStepId, List<Answer>> = emptyMap(),
    val isAddingItem: Boolean = false,
) {
    fun getLog(trekStep: TrekStep) = logs.firstOrNull {
        if (it.pathStepId != null) it.pathStepId == trekStep.pathStepId else it.trekId == trekStep.trekId
    }
    fun getAnswers(trek: TrekStep) = answers[(trek.pathStepId ?: trek.trekId)] ?: emptyList()

    val totalProgress get() = steps.count { it.finishedAt != null }
    val totalSteps get() = steps.size
    val progressRatio get() = totalProgress / (totalSteps.takeIf { it > 0 } ?: 1).toFloat()
}

typealias TrekIdOrPathStepId = String