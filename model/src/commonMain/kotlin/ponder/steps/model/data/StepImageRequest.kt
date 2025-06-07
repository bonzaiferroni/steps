package ponder.steps.model.data

import kotlinx.serialization.Serializable

@Serializable
data class StepImageRequest(
    val stepLabel: String,
    val stepDescription: String?,
    val pathLabel: String?,
    val pathDescription: String?,
    val theme: String?
)

fun StepImageRequest.toAiPrompt() = buildString {
    val request = this@toAiPrompt
    appendLine("We are making an image that represents an action. " +
            "It should not be text but instead clearly represent the action. " +
            "IMPORTANT: The image should be square and the dimensions should be 1024 x 1024 pixels. ")
    request.pathLabel?.let {
        appendLine("\nThis image is part of a guide called '$it'")
    }
    request.pathDescription?.let {
        appendLine("\nHere is more information about the guide:")
        appendLine(it)
    }
    request.theme?.let {
        appendLine("\nHere is the theme or style you should use to generate the image:")
        appendLine(it)
    }
    appendLine("\nHere is the action we need to show: ${request.stepLabel}")
    request.stepDescription?.let {
        appendLine("\nHere more detailed information about the action:")
        appendLine(it)
    }
    appendLine("\nUsing the information above, generate an image that shows this action: ${request.stepLabel}")
}