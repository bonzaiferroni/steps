package ponder.steps.io

import androidx.room.MapColumn
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.Answer
import ponder.steps.model.data.DataType
import ponder.steps.model.data.Question

interface AnswerRepository {
    suspend fun createAnswer(logId: String, questionId: String, value: String, type: DataType): Boolean
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
    fun flowPathQuestionsByTrekId(trekId: String): Flow<Map<String, List<Answer>>>

    fun flowRootAnswers(start: Instant, end: Instant): Flow<Map<String, List<Answer>>>
}