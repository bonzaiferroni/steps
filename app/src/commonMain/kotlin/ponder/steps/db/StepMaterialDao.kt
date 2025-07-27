package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.StepMaterial
import ponder.steps.model.data.StepMaterialId
import ponder.steps.model.data.StepMaterialJoin

@Dao
interface StepMaterialDao {
    @Insert
    suspend fun insert(stepMaterial: StepMaterialEntity): Long

    @Update
    suspend fun update(vararg stepMaterials: StepMaterialEntity): Int

    @Upsert
    suspend fun upsert(vararg stepMaterials: StepMaterialEntity): LongArray

    @Delete
    suspend fun delete(stepMaterial: StepMaterialEntity): Int

    @Query("DELETE FROM StepMaterialEntity WHERE id = :id")
    suspend fun deleteStepMaterialById(id: StepMaterialId): Int

    @Query("SELECT * FROM StepMaterialEntity")
    fun flowAllStepMaterials(): Flow<List<StepMaterialEntity>>

    @Query("SELECT * FROM StepMaterialEntity WHERE id = :stepMaterialId")
    suspend fun readStepMaterialById(stepMaterialId: StepMaterialId): StepMaterial?

    @Query("SELECT * FROM StepMaterialEntity WHERE id = :stepMaterialId")
    fun flowStepMaterial(stepMaterialId: StepMaterialId): Flow<StepMaterial>

    @Query(
        "SELECT sr.*, r.label, r.materialType FROM StepMaterialEntity AS sr " +
                "JOIN MaterialEntity AS r ON sr.materialId = r.id " +
            "WHERE sr.stepId = :stepId")
    fun flowStepMaterialsByStepId(stepId: StepId): Flow<List<StepMaterialJoin>>

    @Query("UPDATE StepMaterialEntity SET quantity = :quantity WHERE id = :stepMaterialId")
    suspend fun updateStepMaterialQuantity(stepMaterialId: StepMaterialId, quantity: Float)
}