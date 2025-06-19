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
import ponder.steps.model.data.TrekId

@Dao
interface AnswerDao {
    @Insert
    suspend fun insert(answer: AnswerEntity)

    @Update
    suspend fun update(vararg answer: AnswerEntity): Int

    @Delete
    suspend fun delete(answer: AnswerEntity): Int

    @Query("SELECT * FROM AnswerEntity WHERE logId = :logId")
    suspend fun readAnswersByLogId(logId: String): List<Answer>

    @Query("SELECT * FROM AnswerEntity WHERE questionId = :questionId")
    suspend fun readAnswersByQuestionId(questionId: String): List<Answer>

    @Query("SELECT * FROM AnswerEntity WHERE logId = :logId AND questionId = :questionId")
    suspend fun readAnswer(logId: String, questionId: String): Answer?

    @Query("SELECT * FROM AnswerEntity WHERE logId = :logId")
    fun flowAnswersByLogId(logId: String): Flow<List<Answer>>

    @Query(
        "SELECT a.*, l.pathStepId FROM StepLogEntity AS l " +
                "JOIN AnswerEntity AS a ON l.id = a.logId " +
                "WHERE l.trekId = :trekId"
    )
    fun flowPathAnswersByTrekId(trekId: String): Flow<Map<@MapColumn("pathStepId") PathStepId, List<Answer>>>

    @Query(
        "SELECT a.*, l.trekId FROM TrekEntity AS t " +
                "JOIN StepLogEntity AS l ON t.id = l.trekId " +
                "JOIN AnswerEntity AS a ON l.id = a.logId " +
                "WHERE t.superId IS NULL AND ((t.availableAt >= :start AND t.availableAt < :end) OR NOT t.isComplete) "
    )
    fun flowRootAnswers(start: Instant, end: Instant): Flow<Map<@MapColumn("trekId") TrekId, List<Answer>>>

    @Query("SELECT * FROM StepLogEntity AS s " +
            "JOIN AnswerEntity AS a ON s.id = a.logId " +
            "WHERE s.stepId = :stepId")
    fun flowAnswersByStepId(stepId: StepId): Flow<Map<StepLog, List<Answer>>>
}