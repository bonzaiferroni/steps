package ponder.steps.model.data

import kotlinx.datetime.Instant

data class CountBucket(
    val count: Int,
    val intervalStart: Instant
)

data class IntBucket(
    val sum: Int,
    val count: Int,
    val intervalStart: Instant
)