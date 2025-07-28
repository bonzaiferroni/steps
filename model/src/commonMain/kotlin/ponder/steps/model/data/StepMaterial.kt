package ponder.steps.model.data

import kabinet.model.LabeledEnum
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

enum class MaterialUnit(override val label: String): LabeledEnum<MaterialUnit> {
    Quantity("quantity"),
    Grams("grams"),
    Liters("liters"),
    Milliliters("milliliters");

    val unitType get () = when (this) {
        Quantity -> UnitType.Quantity
        Grams -> UnitType.Weight
        Liters -> UnitType.Volume
        Milliliters -> UnitType.Volume
    }

    companion object {
        fun fromLabel(label: String): MaterialUnit? = entries.firstOrNull { it.label == label }
    }
}

fun MaterialUnit.defaultQuantity() = when (this) {
    MaterialUnit.Quantity -> 1.0f
    MaterialUnit.Grams -> 100.0f
    MaterialUnit.Liters -> 1.0f
    MaterialUnit.Milliliters -> 100.0f
}