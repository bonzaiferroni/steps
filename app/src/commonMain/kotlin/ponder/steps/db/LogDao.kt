package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.StepOutcome

@Dao
interface LogDao {

    @Insert
    suspend fun insert(logEntry: LogEntryEntity)

    @Update
    suspend fun update(vararg logEntries: LogEntryEntity): Int

    @Delete
    suspend fun delete(logEntry: LogEntryEntity): Int

    @Query("DELETE FROM LogEntryEntity WHERE id = :id")
    suspend fun deleteLogEntryById(id: String): Int

    @Query("SELECT * FROM LogEntryEntity")
    fun getAllLogEntriesAsFlow(): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM LogEntryEntity WHERE id = :logEntryId")
    suspend fun readLogEntryOrNull(logEntryId: String): LogEntryEntity?

    @Query("SELECT * FROM LogEntryEntity WHERE id = :logEntryId")
    fun flowLogEntry(logEntryId: String): Flow<LogEntryEntity>

    suspend fun readLogEntry(logEntryId: String) = readLogEntryOrNull(logEntryId) ?: error("logEntryId missing: $logEntryId")

    @Query("SELECT * FROM LogEntryEntity WHERE stepId = :stepId")
    suspend fun readLogEntriesByStepId(stepId: String): List<LogEntryEntity>

    @Query("SELECT * FROM LogEntryEntity WHERE trekId = :trekId")
    suspend fun readLogEntriesByTrekId(trekId: String): List<LogEntryEntity>

    @Query("SELECT * FROM LogEntryEntity WHERE trekId = :trekId")
    fun flowLogEntriesByTrekId(trekId: String): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM LogEntryEntity WHERE outcome = :outcome")
    suspend fun readLogEntriesByOutcome(outcome: StepOutcome): List<LogEntryEntity>

    @Query("SELECT * FROM LogEntryEntity WHERE createdAt > :startTime AND createdAt < :endTime")
    suspend fun readLogEntriesInTimeRange(startTime: Instant, endTime: Instant): List<LogEntryEntity>

    @Query("SELECT * FROM LogEntryEntity WHERE updatedAt > :lastSyncAt")
    suspend fun readLogEntriesUpdatedAfter(lastSyncAt: Instant): List<LogEntryEntity>
}