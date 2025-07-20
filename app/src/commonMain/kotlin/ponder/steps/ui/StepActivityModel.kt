package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.steps.io.AnswerRepository
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.QuestionSource
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.io.StepLogRepository
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question
import ponder.steps.db.TimeUnit
import ponder.steps.model.data.CountBucket
import ponder.steps.model.data.IntBucket
import pondui.ui.core.StateModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class StepActivityModel(
    private val stepId: String,
    private val stepLogRepo: StepLogRepository = LocalStepLogRepository(),
    private val answerRepo: AnswerRepository = LocalAnswerRepository(),
    private val questionRepo: QuestionRepository = QuestionSource()
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

            setState { it.copy(timeUnit = timeUnit) }

            questionRepo.flowQuestionsByStepId(stepId).collect { questions ->
                setState { it.copy(questions = questions) }
                refreshData()
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            val startAt = Clock.System.now() - stateNow.interval * CHART_MAX_BARS
            val countBuckets = stepLogRepo.readLogCountsByStepId(stepId, startAt, stateNow.timeUnit)
            val intQuestionBuckets = mutableListOf<QuestionBuckets<IntBucket>>()
            for (question in stateNow.questions) {
                if (question.type != DataType.Integer) continue
                val buckets = answerRepo.readIntegerSumsByQuestionId(question.id, startAt, stateNow.timeUnit)
                intQuestionBuckets.add(QuestionBuckets(question, buckets))
            }
            setState { it.copy(intQuestionBuckets = intQuestionBuckets, countBuckets = countBuckets) }
        }
    }

    fun setTimeUnit(timeUnit: TimeUnit) {
        if (stateNow.timeUnit == timeUnit) return
        setState { it.copy(timeUnit = timeUnit) }
        refreshData()
    }
}

data class StepActivityState(
    val questions: List<Question> = emptyList(),
    val countBuckets: List<CountBucket> = emptyList(),
    val intQuestionBuckets: List<QuestionBuckets<IntBucket>> = emptyList(),
    val timeUnit: TimeUnit = TimeUnit.Hour,
) {
    val interval: Duration get() = when (timeUnit) {
        TimeUnit.Minute -> 10.minutes
        else -> timeUnit.toDuration()
    }
}

data class QuestionBuckets<T>(
    val question: Question,
    val buckets: List<T>
)

internal const val CHART_MAX_BARS = 120