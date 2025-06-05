package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class StepImageRequest(
    val stepLabel: String,
    val stepDescription: String?,
    val pathTheme: String?
)

fun StepImageRequest.toAiPrompt() = buildString {
    val request = this@toAiPrompt
    appendLine("We are making an image that represents an action. " +
            "It should not be text but instead clearly represent the action. " +
            "It should be a 1024x1024 pixels. Here is the action we need to show:")
    appendLine(request.stepLabel)
    request.stepDescription?.let {
        appendLine("\nHere more detailed information about the action:")
        appendLine(it)
    }
    request.pathTheme?.let {
        appendLine("\nHere is the theme or style you should use to generate the image:")
        appendLine(it)
    }
}