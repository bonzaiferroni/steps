package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.CountBucket
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekId

@Dao
interface StepLogDao {

    @Insert
    suspend fun insert(stepLogEntity: StepLogEntity)

    @Update
    suspend fun update(vararg stepLogs: StepLogEntity): Int

    @Delete
    suspend fun delete(stepLog: StepLogEntity): Int

    @Query("DELETE FROM StepLogEntity WHERE trekId = :trekId AND stepId = :stepId AND pathStepId = :pathStepId")
    suspend fun deletePathStepLog(trekId: String, stepId: String, pathStepId: String): Int

    @Query("DELETE FROM StepLogEntity WHERE trekId = :trekId AND stepId = :pathId AND pathStepId IS NULL")
    suspend fun deletePathLog(trekId: String, pathId: StepId): Int

    @Query("DELETE FROM StepLogEntity WHERE trekId = :trekId AND stepId IN (:pathIds) AND pathStepId IS NULL")
    suspend fun deletePathLogs(trekId: String, pathIds: List<StepId>): Int

    @Query("DELETE FROM StepLogEntity WHERE id = :id")
    suspend fun deleteStepLogById(id: String): Int

    @Query("SELECT * FROM StepLogEntity")
    fun getAllStepLogsAsFlow(): Flow<List<StepLog>>

    @Query("SELECT * FROM StepLogEntity WHERE id = :stepLogId")
    suspend fun readStepLogOrNull(stepLogId: String): StepLog?

    @Query("SELECT * FROM StepLogEntity WHERE id = :stepLogId")
    fun flowStepLog(stepLogId: String): Flow<StepLog>

    suspend fun readStepLog(stepLogId: String) =
        readStepLogOrNull(stepLogId) ?: error("stepLogId missing: $stepLogId")

    @Query("SELECT * FROM StepLogEntity WHERE stepId = :stepId")
    suspend fun readStepLogsByStepId(stepId: String): List<StepLog>

    @Query("SELECT * FROM StepLogEntity WHERE outcome = :outcome")
    suspend fun readStepLogsByOutcome(outcome: StepOutcome): List<StepLog>

    @Query("SELECT * FROM StepLogEntity WHERE createdAt > :startTime AND createdAt < :endTime")
    suspend fun readStepLogsInTimeRange(startTime: Instant, endTime: Instant): List<StepLog>

    @Query("SELECT * FROM StepLogEntity WHERE updatedAt > :lastSyncAt")
    suspend fun readStepLogsUpdatedAfter(lastSyncAt: Instant): List<StepLog>

    @Query(
        "SELECT l.* FROM TrekEntity AS t " +
                "JOIN StepLogEntity AS l ON t.id = l.trekId " +
                "WHERE l.pathStepId IS NULL AND (t.createdAt >= :start OR NOT t.isComplete) "
    )
    fun flowRootLogs(start: Instant): Flow<List<StepLog>>

    @Query(
        "SELECT l.* FROM TrekEntity AS t " +
                "JOIN StepLogEntity AS l ON t.id = l.trekId " +
                "WHERE t.id IN (:trekIds)"
    )
    fun flowLogsByTrekIds(trekIds: List<TrekId>): Flow<List<StepLog>>

    @Query("SELECT * FROM StepLogEntity WHERE stepId = :stepId")
    fun flowStepLogsByStepId(stepId: StepId): Flow<List<StepLog>>

    @Query(
        "SELECT " +
                "CASE :interval " +
                "WHEN 'Minute' THEN (CAST(strftime('%s', l.createdAt/1000,'unixepoch','localtime')/600 AS INTEGER)*600)*1000 " +
                "WHEN 'Hour'   THEN strftime('%s', strftime('%Y-%m-%d %H:00:00', l.createdAt/1000,'unixepoch','localtime'))*1000 " +
                "WHEN 'Day'    THEN strftime('%s', date(l.createdAt/1000,'unixepoch','localtime'))*1000 " +
                "WHEN 'Week'   THEN strftime('%s', date(l.createdAt/1000,'unixepoch','localtime','weekday 0','-6 days'))*1000 " +
                "WHEN 'Month'  THEN strftime('%s', date(l.createdAt/1000,'unixepoch','localtime','start of month'))*1000 " +
                "WHEN 'Year'   THEN strftime('%s', date(l.createdAt/1000,'unixepoch','localtime','start of year'))*1000 " +
                "END AS intervalStart, " +
                "COUNT(*) AS count " +
                "FROM StepLogEntity AS l " +
                "WHERE l.stepId = :stepId AND l.createdAt >= :startAt " +
                "GROUP BY CASE :interval " +
                "WHEN 'Minute' THEN (CAST(strftime('%s', l.createdAt/1000,'unixepoch','localtime')/600 AS INTEGER)*600)*1000 " +
                "WHEN 'Hour'   THEN strftime('%s', strftime('%Y-%m-%d %H:00:00', l.createdAt/1000,'unixepoch','localtime'))*1000 " +
                "WHEN 'Day'    THEN strftime('%s', date(l.createdAt/1000,'unixepoch','localtime'))*1000 " +
                "WHEN 'Week'   THEN strftime('%s', date(l.createdAt/1000,'unixepoch','localtime','weekday 0','-6 days'))*1000 " +
                "WHEN 'Month'  THEN strftime('%s', date(l.createdAt/1000,'unixepoch','localtime','start of month'))*1000 " +
                "WHEN 'Year'   THEN strftime('%s', date(l.createdAt/1000,'unixepoch','localtime','start of year'))*1000 " +
                "END " +
                "ORDER BY intervalStart"
    )
    suspend fun readLogCountsByStepId(stepId: StepId, startAt: Instant, interval: TimeUnit): List<CountBucket>

    @Query("SELECT MIN(createdAt) FROM StepLogEntity WHERE stepId = :stepId")
    suspend fun readEarliestLogTimeByStepId(stepId: StepId): Instant

    @Query("SELECT l.* FROM PathStepEntity AS ps " +
            "JOIN StepLogEntity AS l ON ps.id = l.pathStepId " +
            "WHERE ps.pathId = :pathId AND l.trekId = :trekId")
    fun flowPathLogsByTrekId(pathId: StepId, trekId: TrekId): Flow<List<StepLog>>

    @Query("SELECT l.* FROM PathStepEntity AS ps " +
            "JOIN StepLogEntity AS l ON ps.id = l.pathStepId " +
            "WHERE l.trekId = :trekId AND ps.pathId = :pathId")
    suspend fun readTrekLogsByPathId(trekId: TrekId, pathId: StepId): List<StepLog>

    @Query("SELECT * FROM StepLogEntity WHERE trekId = :trekId AND stepId = :stepId AND pathStepId IS NULL")
    suspend fun readTopLevelLog(trekId: TrekId, stepId: StepId): List<StepLog>
}
