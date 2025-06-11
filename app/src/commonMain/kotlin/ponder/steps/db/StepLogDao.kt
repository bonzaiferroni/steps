package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

@Dao
interface StepLogDao {
    @Insert
    suspend fun insert(log: StepLogEntity)

    @Update
    suspend fun update(vararg log: StepLogEntity): Int

    @Delete
    suspend fun delete(step: StepLogEntity): Int
}