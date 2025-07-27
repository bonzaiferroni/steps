package ponder.steps.model.data

import kabinet.model.LabeledEnum
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Material(
    val id: MaterialId,
    val label: String,
    val materialType: MaterialType,
    val unitType: UnitType,
    val updatedAt: Instant,
)

typealias MaterialId = String

enum class MaterialType(override val label: String): LabeledEnum<MaterialType> {
    Ingredient("Ingredient"),
    Tool("Tool")
}

enum class UnitType(override val label: String): LabeledEnum<UnitType> {
    Weight("Weight"),
    Volume("Volume"),
    Quantity("Quantity")
}

fun UnitType.defaultQuantityType() = when (this) {
    UnitType.Quantity -> MaterialUnit.Quantity
    UnitType.Volume -> MaterialUnit.Milliliters
    UnitType.Weight -> MaterialUnit.Grams
}