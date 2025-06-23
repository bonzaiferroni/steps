package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.steps.io.AnswerRepository
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.io.StepLogRepository
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question
import ponder.steps.db.TimeUnit
import ponder.steps.model.data.CountBucket
import ponder.steps.model.data.IntBucket
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class StepActivityModel(
    private val stepId: String,
    private val stepLogRepo: StepLogRepository = LocalStepLogRepository(),
    private val answerRepo: AnswerRepository = LocalAnswerRepository(),
    private val questionRepo: QuestionRepository = LocalQuestionRepository()
): StateModel<StepActivityState>(StepActivityState()) {

    init {
        viewModelScope.launch {
            val earliestLogTime = stepLogRepo.readEarliestLogTimeByStepId(stepId)
            val timeSpan = Clock.System.now() - earliestLogTime
            val timeUnit = when {
                2.hours > timeSpan -> TimeUnit.Minute
                2.days > timeSpan -> TimeUnit.Hour
                14.days > timeSpan -> TimeUnit.Day
                60.days > timeSpan -> TimeUnit.Week
                1000.days > timeSpan -> TimeUnit.Month
                else -> TimeUnit.Year
            }
            val countBuckets = stepLogRepo.readLogCountsByStepId(stepId, timeUnit)
            setState { it.copy(countBuckets = countBuckets, timeUnit = timeUnit) }

            questionRepo.flowQuestionsByStepId(stepId).collect { questions ->
                setState { it.copy(questions = questions) }
                readAnswers()
            }
        }

    }

    fun readAnswers() {
        viewModelScope.launch {
            val intQuestionBuckets = mutableListOf<QuestionBuckets<IntBucket>>()
            for (question in stateNow.questions) {
                if (question.type != DataType.Integer) continue
                val buckets = answerRepo.readIntegerSumsByQuestionId(question.id, TimeUnit.Hour)
                intQuestionBuckets.add(QuestionBuckets(question, buckets))
            }
            setState { it.copy(intQuestionBuckets = intQuestionBuckets) }
        }
    }
}

data class StepActivityState(
    val questions: List<Question> = emptyList(),
    val countBuckets: List<CountBucket> = emptyList(),
    val intQuestionBuckets: List<QuestionBuckets<IntBucket>> = emptyList(),
    val timeUnit: TimeUnit = TimeUnit.Hour
)

data class QuestionBuckets<T>(
    val question: Question,
    val buckets: List<T>
)