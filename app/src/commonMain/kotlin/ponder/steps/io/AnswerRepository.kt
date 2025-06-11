package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.Answer
import ponder.steps.model.data.DataType

interface AnswerRepository {
    suspend fun createAnswer(logId: String, questionId: String, value: String, type: DataType): Boolean
    suspend fun readAnswersByLogId(logId: String): List<Answer>
    suspend fun readAnswersByQuestionId(questionId: String): List<Answer>
    suspend fun readAnswer(logId: String, questionId: String): Answer?
    suspend fun updateAnswer(answer: Answer): Boolean
    suspend fun deleteAnswer(answer: Answer): Boolean
    fun flowAnswersByLogId(logId: String): Flow<List<Answer>>
}