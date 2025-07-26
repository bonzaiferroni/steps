package ponder.steps.model.data

import kotlinx.datetime.Instant

data class StepMaterialJoin(
    val id: StepMaterialId,
    val materialId: MaterialId,
    val stepId: StepId,
    val quantity: Float,
    val materialUnit: MaterialUnit,
    val label: String,
    val materialType: MaterialType,
    val updatedAt: Instant,
)