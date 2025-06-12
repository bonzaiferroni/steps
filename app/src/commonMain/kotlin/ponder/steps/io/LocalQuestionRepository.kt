package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import ponder.steps.appDb
import ponder.steps.db.QuestionDao
import ponder.steps.db.QuestionEntity
import ponder.steps.model.data.Question

class LocalQuestionRepository(
    private val questionDao: QuestionDao = appDb.getQuestionDao()
): QuestionRepository {

    override suspend fun readQuestionsByStepId(stepId: String) = questionDao.readQuestionsByStepId(stepId)

    override suspend fun createQuestion(question: QuestionEntity) = questionDao.insert(question)

    override fun flowQuestionsByStepId(stepId: String): Flow<List<Question>> = questionDao.flowQuestionsByStepId(stepId)
}
