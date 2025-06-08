package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.Intent

@Dao
interface IntentDao  {
    @Query("SELECT * FROM IntentEntity WHERE id = :intentId")
    suspend fun readIntentById(intentId: String): Intent

    @Query("SELECT * FROM IntentEntity")
    suspend fun readAllIntents(): List<Intent>

    @Query("SELECT * FROM IntentEntity WHERE completedAt IS NULL")
    fun readActiveIntentsFlow(): Flow<List<Intent>>

    @Query("SELECT id FROM IntentEntity WHERE completedAt IS NULL")
    suspend fun readActiveItentIds(): List<String>

    @Insert
    suspend fun create(intent: IntentEntity)

    @Update
    suspend fun update(vararg intents: IntentEntity): Int

    @Delete
    suspend fun deleteStep(intent: IntentEntity): Int
}