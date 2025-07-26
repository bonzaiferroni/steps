package ponder.steps.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ponder.steps.model.data.Material
import ponder.steps.model.data.MaterialId
import ponder.steps.model.data.MaterialType
import ponder.steps.model.data.QuantityType

@Entity
data class MaterialEntity(
    @PrimaryKey
    val id: MaterialId,
    val label: String,
    val materialType: MaterialType,
    val quantityType: QuantityType,
    val updatedAt: Instant,
)

fun Material.toEntity() = MaterialEntity(
    id = id,
    label = label,
    materialType = materialType,
    quantityType = quantityType,
    updatedAt = updatedAt,
)