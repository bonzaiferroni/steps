package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import ponder.steps.appDb
import ponder.steps.db.AnswerDao
import ponder.steps.db.AnswerEntity
import ponder.steps.model.data.Answer
import ponder.steps.model.data.DataType

class LocalAnswerRepository(
    private val answerDao: AnswerDao = appDb.getAnswerDao()
) : AnswerRepository {

    override suspend fun createAnswer(logId: String, questionId: String, value: String, type: DataType): Boolean {
        val answerEntity = AnswerEntity(logId, questionId, value, type)
        return try {
            answerDao.insert(answerEntity)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun readAnswersByLogId(logId: String): List<Answer> {
        return answerDao.readAnswersByLogId(logId)
    }

    override suspend fun readAnswersByQuestionId(questionId: String): List<Answer> {
        return answerDao.readAnswersByQuestionId(questionId)
    }

    override suspend fun readAnswer(logId: String, questionId: String): Answer? {
        return answerDao.readAnswer(logId, questionId)
    }

    override suspend fun updateAnswer(answer: Answer): Boolean {
        val answerEntity = AnswerEntity(answer.logId, answer.questionId, answer.value, answer.type)
        return try {
            answerDao.update(answerEntity) > 0
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteAnswer(answer: Answer): Boolean {
        val answerEntity = AnswerEntity(answer.logId, answer.questionId, answer.value, answer.type)
        return try {
            answerDao.delete(answerEntity) > 0
        } catch (e: Exception) {
            false
        }
    }

    override fun flowAnswersByLogId(logId: String): Flow<List<Answer>> {
        return answerDao.flowAnswersByLogId(logId)
    }

    override fun flowPathQuestionsByTrekId(trekId: String) = answerDao.flowPathAnswersByTrekId(trekId)
}