package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.PathStep

@Dao
interface StepDao {

    @Query("SELECT * FROM StepEntity")
    fun getAllStepsAsFlow(): Flow<List<StepEntity>>

    @Query("SELECT * FROM StepEntity WHERE id = :stepId")
    suspend fun readStepOrNull(stepId: String): StepEntity?

    suspend fun readStep(stepId: String) = readStepOrNull(stepId) ?: error("stepId missing: $stepId")

//    @Query(
//        "SELECT * FROM StepEntity " +
//                "JOIN PathStepEntity ON StepEntity.id = PathStepEntity.pathId " +
//                "WHERE StepEntity.id = :pathId"
//    )
//    suspend fun readPath(pathId: String): Map<StepEntity, List<PathStepEntity>>

    @Query(
        "SELECT * FROM PathStepEntity " +
                "JOIN StepEntity ON PathStepEntity.stepId = StepEntity.id " +
                "WHERE PathStepEntity.pathId = :pathId"
    )
    suspend fun readPathSteps(pathId: String): List<StepJoin>

    @Query(
        "SELECT * FROM PathStepEntity " +
                "JOIN StepEntity ON PathStepEntity.stepId = StepEntity.id " +
                "WHERE PathStepEntity.pathId = :pathId"
    )
    fun readPathStepsFlow(pathId: String): Flow<List<StepJoin>>

    @Query(
        "SELECT * FROM StepEntity " +
                "WHERE StepEntity.id NOT IN (SELECT stepId FROM PathStepEntity)"
    )
    suspend fun readRootSteps(): List<StepEntity>

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

    @Query("SELECT * FROM StepEntity WHERE label LIKE '%' || :text || '%' ")
    suspend fun searchSteps(text: String): List<StepEntity>

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
}
