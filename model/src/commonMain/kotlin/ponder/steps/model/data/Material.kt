package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: MaterialId,
    val label: String,
    val materialType: MaterialType,
    val quantityType: QuantityType,
    val updatedAt: Instant,
)

typealias MaterialId = String

enum class MaterialType {
    Ingredient,
    Tool
}

enum class QuantityType {
    Quantity,
    Volume,
    Weight
}

fun QuantityType.defaultQuantityType() = when (this) {
    QuantityType.Quantity -> MaterialUnit.Quantity
    QuantityType.Volume -> MaterialUnit.Milliliters
    QuantityType.Weight -> MaterialUnit.Grams
}