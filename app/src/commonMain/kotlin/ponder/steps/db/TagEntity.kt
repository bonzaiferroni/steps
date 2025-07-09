package ponder.steps.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ponder.steps.model.data.Tag
import ponder.steps.model.data.TagId

@Entity
data class TagEntity(
    @PrimaryKey
    val id: TagId,
    val label: String,
    val updatedAt: Instant,
)

fun Tag.toEntity(isUpdated: Boolean = true) = TagEntity(
    id = id,
    label = label,
    updatedAt = if (isUpdated) Clock.System.now() else updatedAt,
)
