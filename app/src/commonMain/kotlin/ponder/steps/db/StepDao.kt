package ponder.steps.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.Step

@Dao
interface StepDao {
    @Insert
    suspend fun insert(step: Step)

    @Query("SELECT * FROM TodoEntity")
    fun getAllAsFlow(): Flow<List<Step>>

    @Query("SELECT * FROM Step")
    fun getAllSteps(): Flow<List<Step>>
}