package ponder.steps.model.data

data class IntentState(
    val intentId: IntentId?,
    val trekId: TrekId?,
    val stepId: StepId?,
    val pathStepId: PathStepId?,
)