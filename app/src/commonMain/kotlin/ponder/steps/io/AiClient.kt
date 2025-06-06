package ponder.steps.io

import ponder.steps.model.Api
import ponder.steps.model.data.StepImageRequest
import ponder.steps.model.data.StepSuggestRequest
import pondui.io.ApiClient
import pondui.io.globalApiClient

class AiClient(private val client: ApiClient = globalApiClient) {
    suspend fun generateImage(stepId: String) = client.get(Api.Steps.GenerateImageV1, stepId)
    suspend fun generateImage(request: StepImageRequest) = client.post(Api.Steps.GenerateImageV2, request)
    suspend fun suggestStep(request: StepSuggestRequest) = client.post(Api.Steps.Suggest, request)
}