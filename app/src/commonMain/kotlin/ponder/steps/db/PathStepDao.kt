package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.PathStep

@Dao
interface PathStepDao {

    @Insert
    suspend fun insert(pathStep: PathStepEntity)

    @Update
    suspend fun update(vararg pathSteps: PathStepEntity): Int

    @Upsert
    suspend fun upsert(vararg pathSteps: PathStepEntity): LongArray

    @Delete
    suspend fun deletePathStep(pathStep: PathStepEntity): Int

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
        "SELECT COUNT(*) " +
                "FROM PathStepEntity " +
                "WHERE pathId = :pathId"
    )
    suspend fun readPathStepCount(pathId: String): Int

    @Query("SELECT * FROM PathStepEntity WHERE pathId = :pathId AND stepId = :stepId")
    suspend fun readPathStep(pathId: String, stepId: String): PathStep

    @Query("SELECT * FROM PathStepEntity WHERE pathId = :pathId AND position = :position")
    suspend fun readPathStepByPosition(pathId: String, position: Int): PathStep?

    @Query("SELECT * FROM PathStepEntity WHERE pathId = :pathId AND stepId = :stepId AND position = :position")
    suspend fun readPathStepAtPosition(pathId: String, stepId: String, position: Int): PathStep?

    @Query("SELECT * FROM PathStepEntity WHERE pathId IN (:pathIds)")
    suspend fun readPathStepsByPathIds(pathIds: List<String>): List<PathStep>
}