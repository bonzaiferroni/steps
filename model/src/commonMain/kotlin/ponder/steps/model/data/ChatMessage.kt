package ponder.steps.model.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val origin: String,
    val content: String,
    val sentAt: Instant = Clock.System.now()
)
