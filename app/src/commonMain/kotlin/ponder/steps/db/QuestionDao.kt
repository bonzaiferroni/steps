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
import ponder.steps.model.data.TrekId

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
        "SELECT DISTINCT q.* FROM TrekEntity AS t " +
                "JOIN PathStepEntity AS p ON t.rootId = p.pathId " +
                "JOIN QuestionEntity AS q ON p.stepId = q.stepId " +
                "WHERE t.id = :trekId"
    )
    fun flowPathQuestionsByTrekId(trekId: String): Flow<Map<@MapColumn("stepId") StepId, List<Question>>>

    @Query(
        "SELECT DISTINCT q.* FROM TrekEntity AS t " +
                "JOIN QuestionEntity AS q ON t.rootId = q.stepId " +
                "WHERE t.createdAt >= :start OR NOT t.isComplete "
    )
    fun flowRootQuestions(start: Instant): Flow<Map<@MapColumn("stepId") StepId, List<Question>>>

    @Query("SELECT DISTINCT * FROM QuestionEntity WHERE stepId IN (:stepIds)")
    fun flowQuestionsByStepIds(stepIds: List<StepId>): Flow<Map<@MapColumn("stepId") StepId, List<Question>>>

    @Query("SELECT q.* FROM PathStepEntity AS ps " +
            "JOIN QuestionEntity AS q ON ps.stepId = q.stepId " +
            "WHERE ps.pathId = :pathId ")
    fun flowPathQuestions(pathId: StepId): Flow<Map<@MapColumn("stepId") StepId, List<Question>>>
}

typealias StepId = String