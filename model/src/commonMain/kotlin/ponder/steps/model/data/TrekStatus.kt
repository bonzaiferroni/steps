package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class TrekStatus(
    val step: Step,
    val isPath: Boolean,
    // val lastStep: Step,
    // val nextStep: Step,
)