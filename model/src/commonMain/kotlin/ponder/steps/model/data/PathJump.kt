package ponder.steps.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class PathJump(
    val id: PathJumpId,
    val fromPathStepId: PathStepId,
    val toPathStepId: PathStepId,
    val questionId: QuestionId,
    val value: String?,
)

@JvmInline
@Serializable
value class PathJumpId(override val value: String): TableId<String>

interface TableId<T> {
    val value: T
}