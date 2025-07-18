package ponder.steps.model.data

import kabinet.model.LabeledEnum

enum class DataType(override val label: String): LabeledEnum<DataType> {
    String("Text"),
    Integer("Number"),
    Decimal("Decimal"),
    Boolean("Boolean"),
    TimeStamp("Time"),
}