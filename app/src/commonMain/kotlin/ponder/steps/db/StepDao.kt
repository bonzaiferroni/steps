package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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

    @Insert
    suspend fun insert(step: StepEntity)

    @Insert
    suspend fun insert(pathStep: PathStepEntity)

    @Update
    suspend fun updateSteps(vararg steps: StepEntity): Int

    @Delete
    suspend fun deleteStep(step: StepEntity): Int
}
