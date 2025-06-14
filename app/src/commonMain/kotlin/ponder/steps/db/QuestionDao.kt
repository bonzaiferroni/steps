package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
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
}
