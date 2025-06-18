package ponder.steps.io

import kabinet.utils.randomUuidStringId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.db.LogDao
import ponder.steps.db.toEntity
import ponder.steps.db.toLogEntry
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome

class LocalLogRepository(
    private val logDao: LogDao = appDb.getLogDao()
) : LogRepository {

    override suspend fun readStepLog(stepLogId: String): StepLog? {
        return logDao.readLogEntryOrNull(stepLogId)?.toLogEntry()
    }

    override fun flowLogEntry(logEntryId: String): Flow<StepLog> {
        return logDao.flowLogEntry(logEntryId).map { it.toLogEntry() }
    }

    override suspend fun readLogEntriesByStepId(stepId: String): List<StepLog> {
        return logDao.readLogEntriesByStepId(stepId).map { it.toLogEntry() }
    }

    override suspend fun readLogEntriesByTrekId(trekId: String): List<StepLog> {
        return logDao.readStepLogsByTrekId(trekId).map { it.toLogEntry() }
    }

    override fun flowStepLogsByTrekId(trekId: String): Flow<List<StepLog>> {
        return logDao.flowLogEntriesByTrekId(trekId).map { entities -> entities.map { it.toLogEntry() } }
    }

    override suspend fun readLogEntriesByOutcome(outcome: StepOutcome): List<StepLog> {
        return logDao.readLogEntriesByOutcome(outcome).map { it.toLogEntry() }
    }

    override suspend fun readLogEntriesInTimeRange(startTime: Instant, endTime: Instant): List<StepLog> {
        return logDao.readLogEntriesInTimeRange(startTime, endTime).map { it.toLogEntry() }
    }

    override suspend fun createStepLog(stepLog: StepLog): String {
        val id = randomUuidStringId()
        val now = Clock.System.now()
        logDao.insert(stepLog.copy(
            id = id,
            createdAt = now,
            updatedAt = now
        ).toEntity())
        return id
    }

    override suspend fun deleteTrekStepLog(stepId: String, trekId: String, pathStepId: String?): Boolean {
        return logDao.delete(stepId, trekId, pathStepId) == 1
    }
}
