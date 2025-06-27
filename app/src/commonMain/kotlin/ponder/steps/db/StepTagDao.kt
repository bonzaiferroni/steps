package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import ponder.steps.model.data.StepTagId
import ponder.steps.model.data.TagId

@Dao
interface StepTagDao {

    @Insert
    suspend fun insert(stepTag: StepTagEntity): Long

    @Upsert
    suspend fun upsert(vararg stepTags: StepTagEntity): LongArray

    @Update
    suspend fun update(vararg stepTags: StepTagEntity): Int

    @Delete
    suspend fun delete(stepTag: StepTagEntity): Int

    @Query("SELECT id FROM StepTagEntity WHERE stepId = :stepId AND tagId = :tagId")
    suspend fun readStepTagId(stepId: StepId, tagId: TagId): StepTagId?

    @Query("DELETE FROM StepTagEntity WHERE stepId = :stepId AND tagId = :tagId")
    suspend fun deleteStepTag(stepId: StepId, tagId: TagId): Int
}