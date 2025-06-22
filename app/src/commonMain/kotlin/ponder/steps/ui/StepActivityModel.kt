package ponder.steps.ui

import ponder.steps.db.IntBucket
import ponder.steps.io.AnswerRepository
import ponder.steps.io.LocalAnswerRepository
import ponder.steps.io.LocalQuestionRepository
import ponder.steps.io.LocalStepLogRepository
import ponder.steps.io.QuestionRepository
import ponder.steps.io.StepLogRepository
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question
import ponder.steps.model.data.QuestionId
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class StepActivityModel(
    private val stepId: String,
    private val stepLogRepo: StepLogRepository = LocalStepLogRepository(),
    private val answerRepo: AnswerRepository = LocalAnswerRepository(),
    private val questionRepo: QuestionRepository = LocalQuestionRepository()
): StateModel<StepActivityState>(StepActivityState()) {

    init {
//        answerRepo.flowAnswersByStepId(stepId).launchCollect { stepLogAnswers ->
//            for ((stepLog, answers) in stepLogAnswers) {
//
//            }
//        }
        questionRepo.flowQuestionsByStepId(stepId).launchCollect { questions ->
            setState { it.copy(questions = questions) }
            questions.firstOrNull { it.type == DataType.Integer }?.let { setQuestion(it.id) }
        }
    }

    fun setQuestion(questionId: QuestionId) {
        answerRepo.flowIntegerSumsByQuestionId(questionId, 5.minutes).launchCollect { buckets ->
            println(buckets)
            setState { it.copy(buckets = buckets) }
        }
    }
}

data class StepActivityState(
    val questions: List<Question> = emptyList(),
    val buckets: List<IntBucket> = emptyList()
)