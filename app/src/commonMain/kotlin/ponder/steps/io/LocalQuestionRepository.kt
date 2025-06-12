package ponder.steps.io

import ponder.steps.appDb
import ponder.steps.db.QuestionDao
import ponder.steps.db.QuestionEntity

class LocalQuestionRepository(
    private val questionDao: QuestionDao = appDb.getQuestionDao()
): QuestionRepository {

    override suspend fun readQuestionsByStepId(stepId: String) = questionDao.readQuestionsByStepId(stepId)

    override suspend fun createQuestion(question: QuestionEntity) = questionDao.insert(question)
}
