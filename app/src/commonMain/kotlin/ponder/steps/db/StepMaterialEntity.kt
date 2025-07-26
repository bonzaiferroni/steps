package ponder.steps.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import ponder.steps.model.data.MaterialId
import ponder.steps.model.data.MaterialUnit
import ponder.steps.model.data.StepId
import ponder.steps.model.data.StepMaterial
import ponder.steps.model.data.StepMaterialId

@Entity(
    foreignKeys = [
        ForeignKey(MaterialEntity::class, ["id"], ["materialId"], ForeignKey.CASCADE),
        ForeignKey(StepEntity::class, ["id"], ["stepId"], ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["materialId"]),
        Index(value = ["stepId"]),
    ]
)
data class StepMaterialEntity(
    @PrimaryKey
    val id: StepMaterialId,
    val materialId: MaterialId,
    val stepId: StepId,
    val quantity: Float,
    val materialUnit: MaterialUnit,
    val updatedAt: Instant,
)

fun StepMaterial.toEntity() = StepMaterialEntity(
    id = id,
    materialId = materialId,
    stepId = stepId,
    quantity = quantity,
    materialUnit = materialUnit,
    updatedAt = updatedAt,
)