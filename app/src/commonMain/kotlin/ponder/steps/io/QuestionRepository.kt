package ponder.steps.io

import ponder.steps.model.data.Question

interface QuestionRepository {
    suspend fun readQuestionsByStepId(stepId: String): List<Question>
}