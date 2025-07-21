package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.PathJump
import ponder.steps.model.data.PathJumpId
import ponder.steps.model.data.PathStepId

@Dao
interface PathJumpDao {

    @Insert
    suspend fun createPathJump(entity: PathJumpEntity)

    @Update
    suspend fun updatePathJump(entity: PathJumpEntity)

    @Delete
    suspend fun deletePathJump(entity: PathJumpEntity)

    @Query("SELECT * FROM PathJumpEntity WHERE id = :pathJumpId")
    suspend fun readById(pathJumpId: PathJumpId): PathJump

    @Query("SELECT pj.* FROM PathJumpEntity AS pj " +
            "JOIN PathStepEntity AS ps ON pj.fromPathStepId = ps.id " +
            "WHERE ps.pathId = :pathId ")
    fun flowByPathId(pathId: StepId): Flow<Map<@MapColumn("fromPathStepId") PathStepId, List<PathJump>>>
}