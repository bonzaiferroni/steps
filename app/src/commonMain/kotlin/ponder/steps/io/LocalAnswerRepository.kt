package ponder.steps.io

import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.db.AnswerDao
import ponder.steps.db.StepId
import ponder.steps.model.data.Answer
import ponder.steps.db.TimeUnit
import ponder.steps.db.toEntity
import ponder.steps.model.data.StepLogId
import ponder.steps.model.data.TrekId
import kotlin.time.Duration

class LocalAnswerRepository(
    private val answerDao: AnswerDao = appDb.getAnswerDao()
) : AnswerRepository {

    override suspend fun readAnswersByLogId(logId: String) = answerDao.readAnswersByLogId(logId)

    override suspend fun readAnswersByQuestionId(questionId: String) =
        answerDao.readAnswersByQuestionId(questionId)

    override suspend fun readAnswer(logId: String, questionId: String) = answerDao.readAnswer(logId, questionId)

    override suspend fun updateAnswer(answer: Answer) = answerDao.update(answer.toEntity()) == 1

    override suspend fun deleteAnswer(answer: Answer) = answerDao.delete(answer.toEntity()) > 0

    override fun flowAnswersByLogId(logId: String) = answerDao.flowAnswersByLogId(logId)

    override fun flowPathQuestionsByTrekId(trekId: String) = answerDao.flowPathAnswersByTrekId(trekId)

    override fun flowRootAnswers(start: Instant, end: Instant) = answerDao.flowRootAnswers(start, end)

    override fun flowAnswersByStepId(stepId: StepId) = answerDao.flowAnswersByStepId(stepId)

    override fun flowIntegerSumsByQuestionId(questionId: String, interval: Duration) =
        answerDao.flowIntegerSumsByQuestionId(questionId, interval)

    override suspend fun readIntegerSumsByQuestionId(questionId: String, startAt: Instant, interval: TimeUnit) =
        answerDao.readIntegerSumsByQuestionId(questionId, startAt, interval)

    fun flowAnswersByTrekIds(trekIds: List<TrekId>) = answerDao.flowAnswersByTrekIds(trekIds)
}