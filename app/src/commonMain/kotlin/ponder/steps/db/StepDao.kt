package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.Step
import ponder.steps.model.data.TrekId

@Dao
interface StepDao {

    @Insert
    suspend fun insert(step: StepEntity): Long

    @Update
    suspend fun update(vararg steps: StepEntity): Int

    @Upsert
    suspend fun upsert(vararg steps: StepEntity): LongArray

    @Delete
    suspend fun delete(step: StepEntity): Int

    @Query("DELETE FROM StepEntity WHERE id = :id")
    suspend fun deleteStepById(id: String): Int

    @Query("SELECT * FROM StepEntity")
    fun getAllStepsAsFlow(): Flow<List<StepEntity>>

    @Query("SELECT * FROM StepEntity WHERE id = :stepId")
    suspend fun readStepOrNull(stepId: String): StepEntity?

    @Query("SELECT * FROM StepEntity WHERE id = :stepId")
    fun flowStep(stepId: String): Flow<Step>

    suspend fun readStep(stepId: String) = readStepOrNull(stepId) ?: error("stepId missing: $stepId")

//    @Query(
//        "SELECT * FROM StepEntity " +
//                "JOIN PathStepEntity ON StepEntity.id = PathStepEntity.pathId " +
//                "WHERE StepEntity.id = :pathId"
//    )
//    suspend fun readPath(pathId: String): Map<StepEntity, List<PathStepEntity>>

    @Query(
        "SELECT * FROM StepEntity " +
                "WHERE StepEntity.id NOT IN (SELECT stepId FROM PathStepEntity) " +
                "ORDER BY updatedAt DESC " +
                "LIMIT :limit"
    )
    suspend fun readRootSteps(limit: Int = 20): List<StepEntity>

    @Query(
        "SELECT * FROM StepEntity " +
                "WHERE StepEntity.id NOT IN (SELECT stepId FROM PathStepEntity) " +
                "ORDER BY updatedAt DESC " +
                "LIMIT :limit"
    )
    fun flowRootSteps(limit: Int = 20): Flow<List<StepEntity>>

    @Query(
        "SELECT stepId FROM PathStepEntity " +
                "WHERE stepId = :stepId"
    )
    suspend fun readPathIdsWithStepId(stepId: String): List<String>

    @Query("SELECT * FROM StepEntity " +
            "WHERE label LIKE '%' || :text || '%' " +
            "ORDER BY updatedAt DESC " +
            "LIMIT :limit")
    suspend fun searchSteps(text: String, limit: Int = 20): List<StepEntity>

    @Query("SELECT * FROM StepEntity " +
            "WHERE label LIKE '%' || :text || '%' " +
            "ORDER BY updatedAt DESC " +
            "LIMIT :limit")
    fun flowSearch(text: String, limit: Int = 20): Flow<List<StepEntity>>

    @Query("SELECT pathSize FROM StepEntity WHERE id = :pathId")
    suspend fun readPathSize(pathId: String): Int

    @Query("SELECT COUNT(*) FROM PathStepEntity WHERE pathId IN (:pathIds)")
    suspend fun readTotalStepCount(pathIds: List<String>): Int

    @Query("SELECT MAX(position) FROM PathStepEntity WHERE pathId = :pathId")
    suspend fun readFinalPosition(pathId: String): Int?

    @Query("SELECT position FROM PathStepEntity WHERE pathId = :pathId AND stepId = :stepId")
    suspend fun readStepPosition(pathId: String, stepId: String): Int

    @Query("SELECT pathId FROM pathstepentity WHERE stepId IN (:stepIds)")
    suspend fun readPathIds(stepIds: List<String>): List<String>

    @Query("SELECT * FROM StepEntity WHERE id IN (:stepIds)")
    fun flowByIds(stepIds: List<StepId>): Flow<List<Step>>

    @Query("SELECT id stepId, thumbUrl url FROM StepEntity WHERE id IN (:stepIds)")
    suspend fun readThumbnails(stepIds: List<StepId>): List<StepImgUrl>
}

data class StepImgUrl(
    val stepId: TrekId,
    val url: String,
)
