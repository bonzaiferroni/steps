package ponder.steps.io

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.db.LogDao
import ponder.steps.db.toLogEntry
import ponder.steps.model.data.LogEntry
import ponder.steps.model.data.StepOutcome

class LocalLogRepository(
    private val logDao: LogDao = appDb.getLogDao()
) : LogRepository {

    override suspend fun getLogEntry(logEntryId: String): LogEntry? {
        return logDao.readLogEntryOrNull(logEntryId)?.toLogEntry()
    }

    override fun flowLogEntry(logEntryId: String): Flow<LogEntry> {
        return logDao.flowLogEntry(logEntryId).map { it.toLogEntry() }
    }

    override suspend fun getLogEntriesByStepId(stepId: String): List<LogEntry> {
        return logDao.readLogEntriesByStepId(stepId).map { it.toLogEntry() }
    }

    override suspend fun getLogEntriesByTrekId(trekId: String): List<LogEntry> {
        return logDao.readLogEntriesByTrekId(trekId).map { it.toLogEntry() }
    }

    override fun flowLogEntriesByTrekId(trekId: String): Flow<List<LogEntry>> {
        return logDao.flowLogEntriesByTrekId(trekId).map { entities -> entities.map { it.toLogEntry() } }
    }

    override suspend fun getLogEntriesByOutcome(outcome: StepOutcome): List<LogEntry> {
        return logDao.readLogEntriesByOutcome(outcome).map { it.toLogEntry() }
    }

    override suspend fun getLogEntriesInTimeRange(startTime: Instant, endTime: Instant): List<LogEntry> {
        return logDao.readLogEntriesInTimeRange(startTime, endTime).map { it.toLogEntry() }
    }
}
