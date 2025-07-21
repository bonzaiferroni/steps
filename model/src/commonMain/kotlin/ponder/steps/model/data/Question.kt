package ponder.steps.model.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Question(
    override val id: QuestionId,
    val stepId: StepId,
    val text: String,
    val type: DataType,
    val minValue: Int? = null,
    val maxValue: Int? = null,
    val audioUrl: String? = null,
    override val updatedAt: Instant
): SyncRecord

typealias QuestionId = String

fun Question.Companion.forStep(stepId: StepId) = Question(
    id = "",
    stepId = stepId,
    text = "",
    type = DataType.String,
    minValue = null,
    maxValue = null,
    audioUrl = null,
    updatedAt = Instant.DISTANT_PAST
)