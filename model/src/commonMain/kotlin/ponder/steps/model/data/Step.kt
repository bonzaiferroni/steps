package ponder.steps.model.data

import androidx.compose.runtime.Stable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class Step(
    val id: String,
    val userId: String?,
    val label: String,
    val description: String?,
    val theme: String?,
    val expectedMins: Int?,
    val imgUrl: String?,
    val thumbUrl: String?,
    val audioUrl: String?,
    val isPublic: Boolean,
    val pathSize: Int,
    val updatedAt: Instant,
    val createdAt: Instant,

    val pathId: String?,
    val position: Int?,
)