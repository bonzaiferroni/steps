package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import ponder.steps.appDb
import ponder.steps.db.QuestionDao
import ponder.steps.db.QuestionEntity
import ponder.steps.db.StepDao
import ponder.steps.db.toEntity
import ponder.steps.model.data.Question

class LocalQuestionRepository(
    private val questionDao: QuestionDao = appDb.getQuestionDao(),
): QuestionRepository {

    override suspend fun readQuestionsByStepId(stepId: String) = questionDao.readQuestionsByStepId(stepId)

    override suspend fun createQuestion(question: Question) = questionDao.insert(question.toEntity()) >= 0

    override suspend fun updateQuestion(question: Question) = questionDao.update(question.toEntity()) == 1

    override suspend fun deleteQuestion(question: Question) = questionDao.delete(question.toEntity()) == 1

    override fun flowQuestionsByStepId(stepId: String): Flow<List<Question>> = questionDao.flowQuestionsByStepId(stepId)
}

