package ponder.steps.io

import ponder.steps.db.QuestionEntity
import ponder.steps.model.data.Question

interface QuestionRepository {
    suspend fun readQuestionsByStepId(stepId: String): List<Question>
    suspend fun createQuestion(question: QuestionEntity)
}
