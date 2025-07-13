package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.Intent
import ponder.steps.model.data.IntentId

@Dao
interface IntentDao  {
    @Query("SELECT * FROM IntentEntity WHERE id = :intentId")
    suspend fun readIntentById(intentId: String): Intent?

    @Query("SELECT * FROM IntentEntity")
    suspend fun readAllIntents(): List<Intent>

    @Query("SELECT * FROM IntentEntity WHERE completedAt IS NULL")
    fun flowActiveIntents(): Flow<List<Intent>>

    @Query("SELECT id FROM IntentEntity WHERE completedAt IS NULL")
    suspend fun readActiveIntentIds(): List<String>

    @Insert
    suspend fun create(intent: IntentEntity)

    @Update
    suspend fun update(vararg intents: IntentEntity): Int

    @Delete
    suspend fun deleteStep(intent: IntentEntity): Int

    @Query("DELETE FROM IntentEntity WHERE id = :intentId")
    suspend fun deleteIntent(intentId: IntentId): Int

    @Query("SELECT * FROM IntentEntity AS i " +
            "JOIN TrekPoint AS tp ON i.id = tp.intentId " +
            "WHERE tp.id = :trekPointId")
    suspend fun readIntentByTrekPointId(trekPointId: TrekPointId): Intent

    @Query("UPDATE IntentEntity SET completedAt = :completedAt WHERE id = :intentId")
    suspend fun updateCompletedAt(intentId: IntentId, completedAt: Instant?): Int
}