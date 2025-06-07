package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ponder.steps.model.data.PathStep
import ponder.steps.model.data.Step

@Dao
interface StepDao {

    @Insert
    suspend fun insert(step: StepEntity)

    @Insert
    suspend fun insert(pathStep: PathStepEntity)

    @Update
    suspend fun update(vararg steps: StepEntity): Int

    @Update
    suspend fun update(vararg pathSteps: PathStepEntity): Int

    @Delete
    suspend fun deleteStep(step: StepEntity): Int

    @Query("DELETE FROM StepEntity WHERE id = :id")
    suspend fun deleteStepById(id: String): Int

    @Delete
    suspend fun deletePathStep(pathStep: PathStepEntity): Int

    @Query("SELECT * FROM StepEntity")
    fun getAllStepsAsFlow(): Flow<List<StepEntity>>

    @Query("SELECT * FROM StepEntity WHERE id = :stepId")
    suspend fun readStepOrNull(stepId: String): StepEntity?

    @Query("SELECT * FROM StepEntity WHERE id = :stepId")
    fun flowStep(stepId: String): Flow<StepEntity>

    suspend fun readStep(stepId: String) = readStepOrNull(stepId) ?: error("stepId missing: $stepId")

//    @Query(
//        "SELECT * FROM StepEntity " +
//                "JOIN PathStepEntity ON StepEntity.id = PathStepEntity.pathId " +
//                "WHERE StepEntity.id = :pathId"
//    )
//    suspend fun readPath(pathId: String): Map<StepEntity, List<PathStepEntity>>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM PathStepEntity " +
                "JOIN StepEntity ON PathStepEntity.stepId = StepEntity.id " +
                "WHERE PathStepEntity.pathId = :pathId"
    )
    suspend fun readPathSteps(pathId: String): List<StepJoin>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM PathStepEntity " +
                "JOIN StepEntity ON PathStepEntity.stepId = StepEntity.id " +
                "WHERE PathStepEntity.pathId = :pathId"
    )
    fun flowPathSteps(pathId: String): Flow<List<StepJoin>>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM PathStepEntity " +
                "JOIN StepEntity ON PathStepEntity.stepId = StepEntity.id " +
                "WHERE PathStepEntity.pathId = :pathId"
    )
    fun readPathStepsFlow(pathId: String): Flow<List<StepJoin>>

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
        "SELECT COUNT(*) " +
                "FROM PathStepEntity " +
                "WHERE pathId = :pathId"
    )
    suspend fun readPathStepCount(pathId: String): Int

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

    @Query("SELECT * FROM PathStepEntity WHERE pathId = :pathId AND stepId = :stepId")
    suspend fun readPathStep(pathId: String, stepId: String): PathStep

    @Query("SELECT * FROM PathStepEntity WHERE pathId = :pathId AND position = :position")
    suspend fun readPathStepByPosition(pathId: String, position: Int): PathStep?

    @Query("SELECT pathSize FROM StepEntity WHERE id = :pathId")
    suspend fun readPathSize(pathId: String): Int

    @Query("SELECT COUNT(*) FROM PathStepEntity WHERE pathId IN (:pathIds)")
    suspend fun readTotalStepCount(pathIds: List<String>): Int

    @Query("SELECT MAX(position) FROM PathStepEntity WHERE pathId = :pathId")
    suspend fun readFinalPosition(pathId: String): Int?

    @Query("SELECT position FROM PathStepEntity WHERE pathId = :pathId AND stepId = :stepId")
    suspend fun readStepPosition(pathId: String, stepId: String): Int

    @Query("SELECT * FROM PathStepEntity WHERE pathId = :pathId AND stepId = :stepId AND position = :position")
    suspend fun readPathStepAtPosition(pathId: String, stepId: String, position: Int): PathStep?

    @Query("SELECT pathId FROM pathstepentity WHERE stepId IN (:stepIds)")
    suspend fun readPathIds(stepIds: List<String>): List<String>

    @Query("SELECT * FROM StepEntity WHERE updatedAt > :lastSyncAt")
    suspend fun readStepsUpdatedAfter(lastSyncAt: Instant): List<StepEntity>

    @Query("SELECT * FROM PathStepEntity WHERE pathId IN (:pathIds)")
    suspend fun readPathStepsByPathIds(pathIds: List<String>): List<PathStep>
}
