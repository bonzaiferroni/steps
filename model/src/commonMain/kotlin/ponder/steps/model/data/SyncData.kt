package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class SyncData(
    val steps: List<Step>,
    val pathSteps: List<PathStep>,
)
