package ponder.steps.io

import ponder.steps.model.Api
import pondui.io.ApiClient
import pondui.io.globalApiClient

class TrekApiRepository(private val client: ApiClient = globalApiClient) {
    suspend fun readUserTreks() = client.get(Api.Journey.UserTreks)
    suspend fun completeStep(trekId: String) = client.post(Api.Journey.CompleteStep, trekId)
    suspend fun startTrek(trekId: String) = client.post(Api.Journey.StartTrek, trekId)
    suspend fun pauseTrek(trekId: String) = client.post(Api.Journey.PauseTrek, trekId)
    suspend fun stepIntoPath(trekId: String) = client.post(Api.Journey.StepIntoPath, trekId)
    suspend fun readFocus() = client.getOrNull(Api.Journey.FocusTrek)
}
