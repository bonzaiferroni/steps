package ponder.steps.io

import ponder.steps.appDb
import ponder.steps.db.QuestionDao

class LocalQuestionRepository(
    private val questionDao: QuestionDao = appDb.getQuestionDao()
): QuestionRepository {

    override suspend fun readQuestionsByStepId(stepId: String) = questionDao.readQuestionsByStepId(stepId)
}