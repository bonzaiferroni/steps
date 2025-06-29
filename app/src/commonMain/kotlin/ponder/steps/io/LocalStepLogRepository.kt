package ponder.steps.io

import kabinet.utils.randomUuidStringId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.db.StepId
import ponder.steps.db.StepLogDao
import ponder.steps.db.TimeUnit
import ponder.steps.db.toEntity
import ponder.steps.db.toStepLog
import ponder.steps.model.data.CountBucket
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekId

class LocalStepLogRepository(
    private val stepLogDao: StepLogDao = appDb.getLogDao()
) : StepLogRepository {

    override suspend fun readStepLog(stepLogId: String): StepLog? {
        return stepLogDao.readStepLogOrNull(stepLogId)
    }

    override fun flowStepLog(stepLogId: String): Flow<StepLog> {
        return stepLogDao.flowStepLog(stepLogId)
    }

    override suspend fun readStepLogsByStepId(stepId: String): List<StepLog> {
        return stepLogDao.readStepLogsByStepId(stepId)
    }

    override suspend fun readStepLogsByOutcome(outcome: StepOutcome): List<StepLog> {
        return stepLogDao.readStepLogsByOutcome(outcome)
    }

    override suspend fun readStepLogsInTimeRange(startTime: Instant, endTime: Instant): List<StepLog> {
        return stepLogDao.readStepLogsInTimeRange(startTime, endTime)
    }

    override suspend fun createStepLog(stepLog: StepLog): String {
        val id = randomUuidStringId()
        val now = Clock.System.now()
        stepLogDao.insert(stepLog.copy(
            id = id,
            createdAt = now,
            updatedAt = now
        ).toEntity())
        return id
    }

    override suspend fun deleteTrekStepLog(trekId: String, stepId: String, pathStepId: String?): Boolean {
        return if (pathStepId != null)
            stepLogDao.delete(trekId, stepId, pathStepId) == 1
        else
            stepLogDao.deleteIfNullPathStepId(trekId, stepId) == 1
    }

    override fun flowPathLogsByTrekId(trekId: String) = stepLogDao.flowPathLogsByTrekId(trekId)

    override fun flowRootLogs(start: Instant, end: Instant) = stepLogDao.flowRootLogs(start, end)

    override fun flowStepLogsByStepId(stepId: StepId) = stepLogDao.flowStepLogsByStepId(stepId)

    override suspend fun readLogCountsByStepId(stepId: StepId, startAt: Instant, interval: TimeUnit) =
        stepLogDao.readLogCountsByStepId(stepId, startAt, interval)

    override suspend fun readEarliestLogTimeByStepId(stepId: StepId) = stepLogDao.readEarliestLogTimeByStepId(stepId)

    fun flowLogsByTrekIds(trekIds: List<TrekId>) = stepLogDao.flowLogsByTrekIds(trekIds)
}
