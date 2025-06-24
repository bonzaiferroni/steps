package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.db.StepId
import ponder.steps.model.data.Answer
import ponder.steps.model.data.DataType
import ponder.steps.model.data.PathStepId
import ponder.steps.model.data.StepLog
import ponder.steps.db.TimeUnit
import ponder.steps.model.data.IntBucket
import ponder.steps.model.data.TrekId
import kotlin.time.Duration

interface AnswerRepository {
    suspend fun readAnswersByLogId(logId: String): List<Answer>
    suspend fun readAnswersByQuestionId(questionId: String): List<Answer>
    suspend fun readAnswer(logId: String, questionId: String): Answer?
    suspend fun updateAnswer(answer: Answer): Boolean
    suspend fun deleteAnswer(answer: Answer): Boolean
    fun flowAnswersByLogId(logId: String): Flow<List<Answer>>

    /**
     * Observes the questions grouped by their corresponding path for a given trek.
     *
     * @param trekId The ID of the trek for which to retrieve the grouped questions.
     * @return A Flow emitting a map where the keys are pathStep IDs and the values are lists of questions associated with each path.
     */
    fun flowPathQuestionsByTrekId(trekId: String): Flow<Map<PathStepId, List<Answer>>>

    fun flowRootAnswers(start: Instant, end: Instant): Flow<Map<TrekId, List<Answer>>>

    fun flowAnswersByStepId(stepId: StepId): Flow<Map<StepLog, List<Answer>>>

    fun flowIntegerSumsByQuestionId(questionId: String, interval: Duration): Flow<List<IntBucket>>

    suspend fun readIntegerSumsByQuestionId(questionId: String, startAt: Instant, interval: TimeUnit): List<IntBucket>
}