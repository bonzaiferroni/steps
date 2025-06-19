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
    suspend fun update(vararg stepLogs: StepLogEntity): Int

    @Delete
    suspend fun delete(stepLog: StepLogEntity): Int

    @Query("DELETE FROM StepLogEntity WHERE trekId = :trekId AND stepId = :stepId AND pathStepId = :pathStepId")
    suspend fun delete(trekId: String, stepId: String, pathStepId: String): Int

    @Query("DELETE FROM StepLogEntity WHERE trekId = :trekId AND stepId = :stepId AND pathStepId IS NULL")
    suspend fun deleteIfNullPathStepId(trekId: String, stepId: String): Int

    @Query("DELETE FROM StepLogEntity WHERE id = :id")
    suspend fun deleteStepLogById(id: String): Int

    @Query("SELECT * FROM StepLogEntity")
    fun getAllStepLogsAsFlow(): Flow<List<StepLogEntity>>

    @Query("SELECT * FROM StepLogEntity WHERE id = :stepLogId")
    suspend fun readStepLogOrNull(stepLogId: String): StepLogEntity?

    @Query("SELECT * FROM StepLogEntity WHERE id = :stepLogId")
    fun flowStepLog(stepLogId: String): Flow<StepLogEntity>

    suspend fun readStepLog(stepLogId: String) =
        readStepLogOrNull(stepLogId) ?: error("stepLogId missing: $stepLogId")

    @Query("SELECT * FROM StepLogEntity WHERE stepId = :stepId")
    suspend fun readStepLogsByStepId(stepId: String): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE trekId = :trekId")
    suspend fun readStepLogsByTrekId(trekId: String): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE outcome = :outcome")
    suspend fun readStepLogsByOutcome(outcome: StepOutcome): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE createdAt > :startTime AND createdAt < :endTime")
    suspend fun readStepLogsInTimeRange(startTime: Instant, endTime: Instant): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE updatedAt > :lastSyncAt")
    suspend fun readStepLogsUpdatedAfter(lastSyncAt: Instant): List<StepLogEntity>

    @Query("SELECT * FROM StepLogEntity WHERE trekId = :trekId")
    fun flowPathLogsByTrekId(trekId: String): Flow<List<StepLog>>

    @Query(
        "SELECT l.* FROM TrekEntity AS t " +
                "JOIN StepLogEntity AS l ON t.id = l.trekId " +
                "WHERE t.superId IS NULL AND ((t.availableAt > :start AND t.availableAt < :end) OR NOT t.isComplete) "
    )
    fun flowRootLogs(start: Instant, end: Instant): Flow<List<StepLog>>

    @Query("SELECT * FROM StepLogEntity WHERE stepId = :stepId")
    fun flowStepLogsByStepId(stepId: StepId): Flow<List<StepLog>>
}
