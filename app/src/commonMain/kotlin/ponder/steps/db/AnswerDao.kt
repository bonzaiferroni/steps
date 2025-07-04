package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.Answer
import ponder.steps.model.data.PathStepId
import ponder.steps.model.data.StepLog
import ponder.steps.model.data.StepLogId
import ponder.steps.model.data.IntBucket
import ponder.steps.model.data.TrekId
import kotlin.time.Duration

@Dao
interface AnswerDao {
    @Insert
    suspend fun insert(answer: AnswerEntity): Long

    @Update
    suspend fun update(vararg answer: AnswerEntity): Int

    @Delete
    suspend fun delete(answer: AnswerEntity): Int

    @Query("SELECT * FROM AnswerEntity WHERE stepLogId = :logId")
    suspend fun readAnswersByLogId(logId: StepLogId): List<Answer>

    @Query("SELECT * FROM AnswerEntity WHERE stepLogId IN (:logIds)")
    suspend fun readAnswersByLogIds(logIds: List<StepLogId>): List<Answer>

    @Query("SELECT * FROM AnswerEntity WHERE questionId = :questionId")
    suspend fun readAnswersByQuestionId(questionId: String): List<Answer>

    @Query("SELECT * FROM AnswerEntity WHERE stepLogId = :logId AND questionId = :questionId")
    suspend fun readAnswer(logId: String, questionId: String): Answer?

    @Query("SELECT * FROM AnswerEntity WHERE stepLogId = :logId")
    fun flowAnswersByLogId(logId: String): Flow<List<Answer>>

    @Query(
        "SELECT a.* FROM TrekEntity AS t " +
                "JOIN StepLogEntity AS l ON t.id = l.trekId " +
                "JOIN AnswerEntity AS a ON l.id = a.stepLogId " +
                "WHERE t.createdAt > :start OR NOT t.isComplete "
    )
    fun flowRootAnswers(start: Instant): Flow<Map<@MapColumn("stepLogId") StepLogId, List<Answer>>>

    @Query("SELECT a.* FROM StepLogEntity AS l " +
            "JOIN AnswerEntity AS a ON l.id = a.stepLogId " +
            "WHERE l.trekId IN (:trekIds)")
    fun flowAnswersByTrekIds(trekIds: List<TrekId>): Flow<Map<@MapColumn("stepLogId") StepLogId, List<Answer>>>

    @Query(
        "SELECT * FROM StepLogEntity AS s " +
                "JOIN AnswerEntity AS a ON s.id = a.stepLogId " +
                "WHERE s.stepId = :stepId"
    )
    fun flowAnswersByStepId(stepId: StepId): Flow<Map<StepLog, List<Answer>>>

    @Query(
        "SELECT " +
                "(l.createdAt/:interval)*:interval AS intervalStart, " +
                "SUM(CAST(a.value AS INTEGER)) AS sum, " +
                "COUNT(*) AS count " +
                "FROM AnswerEntity AS a " +
                "JOIN StepLogEntity AS l ON a.stepLogId = l.id " +
                "WHERE a.questionId = :questionId AND a.type = 'Integer' " +
                "GROUP BY intervalStart " +
                "ORDER BY intervalStart"
    )
    fun flowIntegerSumsByQuestionId(questionId: String, interval: Duration): Flow<List<IntBucket>>

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
                "SUM(CAST(a.value AS INTEGER)) AS sum, " +
                "COUNT(*) AS count " +
                "FROM AnswerEntity AS a " +
                "JOIN StepLogEntity AS l ON a.stepLogId = l.id " +
                "WHERE a.questionId = :questionId AND a.type = 'Integer' AND l.createdAt >= :startAt " +
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
    suspend fun readIntegerSumsByQuestionId(questionId: String, startAt: Instant, interval: TimeUnit): List<IntBucket>

    @Query("SELECT a.* FROM PathStepEntity AS ps " +
            "JOIN StepLogEntity AS l ON ps.id = l.pathStepId " +
            "JOIN AnswerEntity AS a ON l.id = a.stepLogId " +
            "WHERE ps.pathId = :pathId AND l.trekId = :trekId")
    fun flowPathAnswersByTrekId(pathId: StepId, trekId: TrekId): Flow<Map<@MapColumn("stepLogId") StepLogId, List<Answer>>>
}
