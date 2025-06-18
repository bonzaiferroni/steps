package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome

@Dao
interface StepLogDao {

    @Insert
    suspend fun insert(stepLogEntity: StepLogEntity)

    @Update
    suspend fun update(vararg logEntries: StepLogEntity): Int

    @Delete
    suspend fun delete(logEntry: StepLogEntity): Int

    @Query("DELETE FROM StepLogEntity WHERE trekId = :trekId AND stepId = :stepId AND pathStepId = :pathStepId")
    suspend fun delete(trekId: String, stepId: String, pathStepId: String): Int

    @Query("DELETE FROM StepLogEntity WHERE trekId = :trekId AND stepId = :stepId AND pathStepId IS NULL")
    suspend fun deleteIfNullPathStepId(trekId: String, stepId: String): Int

    @Query("DELETE FROM StepLogEntity WHERE id = :id")
    suspend fun deleteLogEntryById(id: String): Int

    @Query("SELECT * FROM StepLogEntity")
    fun getAllLogEntriesAsFlow(): Flow<List<StepLogEntity>>

    @Query("SELECT * FROM StepLogEntity WHERE id = :logEntryId")
    suspend fun readLogEntryOrNull(logEntryId: String): StepLogEntity?

    @Query("SELECT * FROM StepLogEntity WHERE id = :logEntryId")
    fun flowLogEntry(logEntryId: String): Flow<StepLogEntity>

    suspend fun readLogEntry(logEntryId: String) =
        readLogEntryOrNull(logEntryId) ?: error("logEntryId missing: $logEntryId")

    @Query("SELECT * FROM StepLogEntity WHERE stepId = :stepId")
    suspend fun readLogEntriesByStepId(stepId: String): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE trekId = :trekId")
    suspend fun readStepLogsByTrekId(trekId: String): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE outcome = :outcome")
    suspend fun readLogEntriesByOutcome(outcome: StepOutcome): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE createdAt > :startTime AND createdAt < :endTime")
    suspend fun readLogEntriesInTimeRange(startTime: Instant, endTime: Instant): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE updatedAt > :lastSyncAt")
    suspend fun readLogEntriesUpdatedAfter(lastSyncAt: Instant): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE trekId = :trekId")
    fun flowPathLogsByTrekId(trekId: String): Flow<List<StepLog>>

    @Query(
        "SELECT l.* FROM TrekEntity AS t " +
                "JOIN StepLogEntity AS l ON t.id = l.trekId " +
                "WHERE t.superId IS NULL AND ((t.availableAt > :start AND t.availableAt < :end) OR NOT t.isComplete) "
    )
    fun flowRootLogs(start: Instant, end: Instant): Flow<List<StepLog>>
}