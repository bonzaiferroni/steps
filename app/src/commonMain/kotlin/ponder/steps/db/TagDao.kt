package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.Tag
import ponder.steps.model.data.TagId

@Dao
interface TagDao {

    @Insert
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(vararg tags: TagEntity): Int

    @Upsert
    suspend fun upsert(vararg tags: TagEntity): LongArray

    @Delete
    suspend fun delete(tag: TagEntity): Int

    @Query("SELECT t.* FROM StepTagEntity AS st " +
            "JOIN TagEntity AS t ON st.tagId = t.id " +
            "WHERE st.stepId = :stepId ")
    fun flowTagsByStepId(stepId: StepId): Flow<List<Tag>>

    @Query("SELECT id FROM TagEntity WHERE LOWER(label) = LOWER(:label)")
    suspend fun readTagIdByLabel(label: String): TagId?
}