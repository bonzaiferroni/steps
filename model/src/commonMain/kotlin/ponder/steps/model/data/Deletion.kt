package ponder.steps.model.data

import kotlinx.datetime.Instant

data class Deletion(
    val id: String,
    val recordedAt: Instant
)