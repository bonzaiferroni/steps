package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.Question

@Dao
interface QuestionDao {
    @Insert
    suspend fun insert(log: QuestionEntity): Long

    @Update
    suspend fun update(vararg log: QuestionEntity): Int

    @Delete
    suspend fun delete(step: QuestionEntity): Int

    @Query("SELECT * FROM QuestionEntity WHERE stepId = :stepId")
    suspend fun readQuestionsByStepId(stepId: String): List<Question>

    @Query("SELECT * FROM QuestionEntity WHERE stepId = :stepId")
    fun flowQuestionsByStepId(stepId: String): Flow<List<Question>>

    @Query("SELECT * FROM QuestionEntity WHERE stepId IN (:stepIds)")
    suspend fun readQuestionsByStepIds(stepIds: List<String>): List<Question>

    @Query(
        "SELECT q.* FROM TrekEntity AS t " +
                "JOIN PathStepEntity AS p ON t.rootId = p.pathId " +
                "JOIN QuestionEntity AS q ON p.stepId = q.stepId " +
                "WHERE t.id = :trekId"
    )
    fun flowPathQuestionsByTrekId(trekId: String): Flow<Map<@MapColumn("stepId") String, List<Question>>>

    @Query(
        "SELECT q.* FROM TrekEntity AS t " +
                "JOIN StepEntity AS s ON t.rootId = s.id " +
                "JOIN QuestionEntity AS q ON s.id = q.stepId " +
                "WHERE t.superId IS NULL AND ((t.availableAt > :start AND t.availableAt < :end) OR NOT t.isComplete) "
    )
    fun flowRootQuestions(start: Instant, end: Instant): Flow<Map<@MapColumn("stepId") String, List<Question>>>
}
