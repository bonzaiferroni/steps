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
    suspend fun insert(stepLogEntity: StepLogEntity)

    @Update
    suspend fun update(vararg logEntries: StepLogEntity): Int

    @Delete
    suspend fun delete(logEntry: StepLogEntity): Int

    @Query("DELETE FROM StepLogEntity WHERE stepId = :stepId AND trekId = :trekId AND pathStepId = :pathStepId")
    suspend fun delete(stepId: String, trekId: String, pathStepId: String?): Int

    @Query("DELETE FROM StepLogEntity WHERE id = :id")
    suspend fun deleteLogEntryById(id: String): Int

    @Query("SELECT * FROM StepLogEntity")
    fun getAllLogEntriesAsFlow(): Flow<List<StepLogEntity>>

    @Query("SELECT * FROM StepLogEntity WHERE id = :logEntryId")
    suspend fun readLogEntryOrNull(logEntryId: String): StepLogEntity?

    @Query("SELECT * FROM StepLogEntity WHERE id = :logEntryId")
    fun flowLogEntry(logEntryId: String): Flow<StepLogEntity>

    suspend fun readLogEntry(logEntryId: String) = readLogEntryOrNull(logEntryId) ?: error("logEntryId missing: $logEntryId")

    @Query("SELECT * FROM StepLogEntity WHERE stepId = :stepId")
    suspend fun readLogEntriesByStepId(stepId: String): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE trekId = :trekId")
    suspend fun readStepLogsByTrekId(trekId: String): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE trekId = :trekId")
    fun flowLogEntriesByTrekId(trekId: String): Flow<List<StepLogEntity>>

    @Query("SELECT * FROM StepLogEntity WHERE outcome = :outcome")
    suspend fun readLogEntriesByOutcome(outcome: StepOutcome): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE createdAt > :startTime AND createdAt < :endTime")
    suspend fun readLogEntriesInTimeRange(startTime: Instant, endTime: Instant): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE updatedAt > :lastSyncAt")
    suspend fun readLogEntriesUpdatedAfter(lastSyncAt: Instant): List<StepLogEntity>
}