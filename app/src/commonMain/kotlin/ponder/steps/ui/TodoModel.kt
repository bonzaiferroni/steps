package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.replace
import kabinet.utils.startOfDay
import kabinet.utils.startOfWeek
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalLogRepository
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.AnswerRepository
import ponder.steps.io.LogRepository
import ponder.steps.io.TrekRepository
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.Question
import ponder.steps.model.data.TrekItem
import pondui.ui.core.StateModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class TodoModel(
    private val trekRepo: TrekRepository = LocalTrekRepository(),
    private val questionRepo: LocalQuestionRepository = LocalQuestionRepository(),
    private val logRepo: LogRepository = LocalLogRepository(),
    private val answerRepo: AnswerRepository = LocalAnswerRepository()
) : StateModel<TodoState>(TodoState()) {

    private var flowJob: Job? = null

    fun onLoad() {
        flowJob?.cancel()
        flowJob = viewModelScope.launch {
            val startTime = when (stateNow.span) {
                TrekSpan.Hours -> Clock.startOfDay()
                TrekSpan.Day -> Clock.startOfDay()
                TrekSpan.Week -> Clock.startOfWeek()
            }
            val endTime = startTime + stateNow.span.duration
            trekRepo.flowTreksInRange(startTime, endTime).collect { treks ->
                setState {
                    it.copy(
                        items = treks.sortedWith(
                            compareByDescending<TrekItem> { trek -> trek.finishedAt ?: Instant.DISTANT_FUTURE }
                                .thenBy { trek -> trek.priority.ordinal }
                        ))
                }
            }
        }
    }

    fun onDispose() = cancelJobs()

    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun completeStep(item: TrekItem, outcome: StepOutcome) {
        viewModelScope.launch {
//            val logId = trekRepo.setOutcome(item.trekId, null, null, outcome) ?: error("error completing step: ${item.stepLabel}")
//            if (outcome == StepOutcome.Completed) {
//                val questions = questionRepo.readQuestionsByStepId(item.stepId)
//                if (questions.isNotEmpty()) {
//                    modifyQuestionSetState(QuestionSet(item.trekId, logId, questions))
//                    return@launch
//                }
//            }
//            if (trekRepo.isFinished(item.trekId)) {
//                trekRepo.completeTrek(item.trekId)
//            }
        }
    }

    fun setSpan(span: TrekSpan) {
        if (stateNow.span == span) return
        setState { it.copy(span = span) }
        onLoad() // Reload items for the new span
    }

    fun answerQuestion(trekId: String, question: Question, answerText: String?) {
        var questionSet = stateNow.questionSets.firstOrNull { it.trekId == trekId } ?: error("Question set not found")
        val questions = questionSet.questions.filter { it.id != question.id }
        questionSet = questionSet.copy(questions = questions)
        if (answerText == null) {
            modifyQuestionSetState(questionSet)
            return
        }
        viewModelScope.launch {
            val success = answerRepo.createAnswer(questionSet.logId, question.id, answerText, question.type)
            if (success) {
                modifyQuestionSetState(questionSet)
                if (questionSet.questions.isEmpty() && trekRepo.isFinished(trekId)) {
                    trekRepo.completeTrek(trekId)
                }
            }
        }
    }

    private fun modifyQuestionSetState(questionSet: QuestionSet) {
        if (questionSet.questions.isEmpty()) {
            setState { it.copy(questionSets = it.questionSets.filter { set -> set.trekId != questionSet.trekId }) }
        } else if (stateNow.questionSets.all { it.trekId != questionSet.trekId }) {
            setState { it.copy(questionSets = it.questionSets + questionSet) }
        } else {
            setState { it.copy(questionSets = it.questionSets.replace(questionSet) { set -> set.trekId == questionSet.trekId }) }
        }
    }
}

data class TodoState(
    val items: List<TrekItem> = emptyList(),
    val questionSets: List<QuestionSet> = emptyList(),
    val isAddingItem: Boolean = false,
    val span: TrekSpan = TrekSpan.Day
)

enum class TrekSpan(val label: String, val duration: Duration) {
    Hours("4 hours", 4.hours),
    Day("Day", 1.days),
    Week("Week", 7.days);
}

data class QuestionSet(
    val trekId: String,
    val logId: String,
    val questions: List<Question>
)
