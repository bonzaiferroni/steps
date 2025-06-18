package ponder.steps.io

import kabinet.utils.randomUuidStringId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.db.StepLogDao
import ponder.steps.db.toEntity
import ponder.steps.db.toLogEntry
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome

class LocalStepLogRepository(
    private val stepLogDao: StepLogDao = appDb.getLogDao()
) : StepLogRepository {

    override suspend fun readStepLog(stepLogId: String): StepLog? {
        return stepLogDao.readLogEntryOrNull(stepLogId)?.toLogEntry()
    }

    override fun flowLogEntry(logEntryId: String): Flow<StepLog> {
        return stepLogDao.flowLogEntry(logEntryId).map { it.toLogEntry() }
    }

    override suspend fun readLogEntriesByStepId(stepId: String): List<StepLog> {
        return stepLogDao.readLogEntriesByStepId(stepId).map { it.toLogEntry() }
    }

    override suspend fun readLogEntriesByOutcome(outcome: StepOutcome): List<StepLog> {
        return stepLogDao.readLogEntriesByOutcome(outcome).map { it.toLogEntry() }
    }

    override suspend fun readLogEntriesInTimeRange(startTime: Instant, endTime: Instant): List<StepLog> {
        return stepLogDao.readLogEntriesInTimeRange(startTime, endTime).map { it.toLogEntry() }
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
}
