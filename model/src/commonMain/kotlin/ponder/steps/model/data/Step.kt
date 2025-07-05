package ponder.steps.model.data

import kabinet.model.UserId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Step(
    val id: StepId,
    val userId: UserId,
    val label: String,
    val description: String?,
    val theme: String?,
    val expectedMins: Int?,
    val imgUrl: String?,
    val thumbUrl: String?,
    val audioLabelUrl: String?,
    val audioFullUrl: String?,
    val isPublic: Boolean,
    val pathSize: Int,
    val updatedAt: Instant,
    val createdAt: Instant,

    val pathId: String? = null,
    val pathStepId: String? = null,
    val position: Int? = null,
): SyncRecord

typealias StepId = String