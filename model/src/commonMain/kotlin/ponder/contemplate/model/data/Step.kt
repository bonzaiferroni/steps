package ponder.contemplate.model.data

data class Step(
    val id: Int,
    val parentId: Int,
    val label: String,
    val position: Int,
)