package ponder.steps.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.PathStep
import ponder.steps.model.data.Step

@Dao
interface StepDao {

    @Query("SELECT * FROM step")
    fun getAllStepsAsFlow(): Flow<List<Step>>

    @Query("SELECT * FROM step WHERE id = :stepId")
    suspend fun readStep(stepId: String): Step

    @Query(
        "SELECT * FROM step " +
                "JOIN path_step ON step.id = path_step.pathId " +
                "WHERE step.id = :pathId"
    )
    suspend fun readPath(pathId: String): Map<Step, List<PathStep>>

    @Query(
        "SELECT * FROM path_step " +
                "JOIN step ON path_step.stepId = step.id " +
                "WHERE path_step.pathId = :pathId"
    )
    suspend fun readPathSteps(pathId: String): Map<PathStep, Step>

    @Query("""
        SELECT * FROM step
        JOIN path_step ON step.id = path_step.pathId
        WHERE step.id NOT IN (
            SELECT stepId
            FROM path_step
        )
    """)
    suspend fun readRootSteps(): Map<Step, List<PathStep>>

    @Insert
    suspend fun insert(step: Step): String

    @Update
    suspend fun updateSteps(vararg steps: Step): Int
}