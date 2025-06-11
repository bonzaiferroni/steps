package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.Answer

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
}