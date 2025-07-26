package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class StepMaterial(
    val id: StepMaterialId,
    val materialId: MaterialId,
    val stepId: StepId,
    val quantity: Float,
    val materialUnit: MaterialUnit,
    val updatedAt: Instant,
)

typealias StepMaterialId = String

enum class MaterialUnit(val quantityType: QuantityType) {
    Quantity(QuantityType.Quantity),
    Grams(QuantityType.Weight),
    Liters(QuantityType.Volume),
    Milliliters(QuantityType.Volume),
}

fun MaterialUnit.defaultQuantity() = when (this) {
    MaterialUnit.Quantity -> 1.0f
    MaterialUnit.Grams -> 100.0f
    MaterialUnit.Liters -> 1.0f
    MaterialUnit.Milliliters -> 100.0f
}