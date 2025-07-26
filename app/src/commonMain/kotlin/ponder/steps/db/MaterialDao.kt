package ponder.steps.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ponder.steps.model.data.Material
import ponder.steps.model.data.MaterialId

@Dao
interface MaterialDao {
    @Insert
    suspend fun insert(material: MaterialEntity): Long

    @Update
    suspend fun update(vararg materials: MaterialEntity): Int

    @Upsert
    suspend fun upsert(vararg materials: MaterialEntity): LongArray

    @Delete
    suspend fun delete(material: MaterialEntity): Int

    @Query("DELETE FROM MaterialEntity WHERE id = :id")
    suspend fun deleteMaterialById(id: MaterialId): Int

    @Query("SELECT * FROM MaterialEntity")
    fun getAllMaterialsAsFlow(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM MaterialEntity WHERE id = :materialId")
    suspend fun readMaterialById(materialId: MaterialId): Material?

    @Query("SELECT * FROM MaterialEntity WHERE id = :materialId")
    fun flowMaterial(materialId: MaterialId): Flow<Material>

    @Query(
        "SELECT * FROM MaterialEntity " +
            "WHERE label LIKE '%' || :label || '%' COLLATE NOCASE " +
            "ORDER BY LENGTH(label) ASC")
    suspend fun searchMaterialsByLabel(label: String): List<Material>
}