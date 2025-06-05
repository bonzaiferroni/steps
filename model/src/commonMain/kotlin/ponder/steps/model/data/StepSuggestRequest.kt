package ponder.steps.model.data

import kabinet.clients.promptFromTemplate
import kotlinx.serialization.Serializable

@Serializable
data class StepSuggestRequest(
    val pathLabel: String,
    val pathDescription: String?,
    val precedingSteps: List<StepWithDescription>
)

@Serializable
data class StepWithDescription(
    val label: String,
    val description: String?
)

@Serializable
data class StepSuggestGeminiResponse(
    val suggestions: List<String>
)

@Serializable
data class StepSuggestResponse(
    val suggestedSteps: List<StepWithDescription>
)

fun StepSuggestRequest.toAiPrompt(template: String) = promptFromTemplate(
    template,
    "suggestion_count" to "three",
    "objective" to pathLabel,
    "objective_description" to (pathDescription ?: "(No description provided)"),
    "steps_taken" to precedingSteps.joinToString("\n") { step -> "* " + step.label + (step.description?.let { ":$it" } ?: "") }
)