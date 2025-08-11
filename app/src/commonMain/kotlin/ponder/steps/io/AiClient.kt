package ponder.steps.io

import ponder.steps.model.Api
import kabinet.model.SpeechRequest
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepImageRequest
import ponder.steps.model.data.StepSuggestRequest
import pondui.io.ApiClient
import pondui.io.globalApiClient

class AiClient(private val client: ApiClient = globalApiClient) {
    suspend fun generateImage(stepId: String) = client.get(Api.Steps.GenerateImageV1, stepId)

    suspend fun generateImage(step: Step, path: Step?, defaultTheme: String?) = client.post(
        endpoint = Api.Steps.GenerateImageV2,
        value = StepImageRequest(
            stepLabel = step.label,
            stepDescription = step.description,
            pathLabel = path?.label,
            pathDescription = path?.description,
            theme = step.theme ?: path?.theme ?: defaultTheme?.takeIf { it.isNotEmpty() }
        )
    )

    suspend fun generateSpeech(request: SpeechRequest) = client.post(Api.Gemini.GenerateSpeech, request)

    suspend fun suggestStep(request: StepSuggestRequest) = client.post(Api.Steps.Suggest, request)
}
