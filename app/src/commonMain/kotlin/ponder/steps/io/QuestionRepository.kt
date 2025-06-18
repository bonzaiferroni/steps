package ponder.steps.io

import androidx.room.MapColumn
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.db.QuestionEntity
import ponder.steps.model.data.Question

interface QuestionRepository {
    suspend fun readQuestionsByStepId(stepId: String): List<Question>
    suspend fun createQuestion(question: Question): Boolean
    suspend fun updateQuestion(question: Question): Boolean
    suspend fun deleteQuestion(question: Question): Boolean
    fun flowQuestionsByStepId(stepId: String): Flow<List<Question>>

    /**
     * Provides a continuous stream of questions grouped by their pathStep IDs for a given trek ID.
     *
     * @param trekId The ID of the trek for which the questions should be streamed.
     * @return A Flow emitting a map where the key is the pathStep ID and the value is a list of questions associated with that step.
     */
    fun flowPathQuestionsByTrekId(trekId: String): Flow<Map<String, List<Question>>>

    fun flowRootQuestions(start: Instant, end: Instant): Flow<Map<String, List<Question>>>
}
