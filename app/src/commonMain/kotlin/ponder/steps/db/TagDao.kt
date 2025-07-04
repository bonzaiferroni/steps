package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
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

    @Query(
        "SELECT t.* FROM StepTagEntity AS st " +
                "JOIN TagEntity AS t ON st.tagId = t.id " +
                "WHERE st.stepId = :stepId "
    )
    fun flowTagsByStepId(stepId: StepId): Flow<List<Tag>>

    @Query("SELECT id FROM TagEntity WHERE LOWER(label) = LOWER(:label)")
    suspend fun readTagIdByLabel(label: String): TagId?

    @Query(
        "SELECT t.id tagId, t.label label, COUNT(s.stepId) count " +
                "FROM StepTagEntity AS s " +
                "JOIN TagEntity     AS t ON t.id = s.tagId " +
                "GROUP BY s.tagId, t.label " +
                "ORDER BY COUNT(s.stepId) DESC " +
                "LIMIT :limit"
    )
    fun flowTopTagCounts(limit: Int): Flow<List<TagCount>>

    @Query("SELECT t.*, st.stepId FROM StepTagEntity AS st " +
            "JOIN TagEntity AS t ON st.tagId = t.id " +
            "WHERE st.stepId IN (:stepIds)")
    fun flowTagsByStepIds(stepIds: List<StepId>): Flow<Map<@MapColumn("stepId") StepId, List<Tag>>>

    @Query("SELECT tag.*, st.stepId FROM TagEntity AS tag " +
            "JOIN StepTagEntity AS st ON tag.id = st.tagId " +
            "JOIN TrekEntity AS t ON t.rootId = st.stepId " +
            "WHERE t.createdAt > :start OR NOT t.isComplete"
    )
    fun flowRootTags(start: Instant): Flow<Map<@MapColumn("stepId") StepId, List<Tag>>>
}

data class TagCount(
    val tagId: String,
    val label: String,
    val count: Int
)