package ponder.steps.io

import ponder.steps.model.Api
import pondui.io.ApiStore

class TrekApiStore: ApiStore() {
    suspend fun readUserTreks() = client.get(Api.Journey.UserTreks)
    suspend fun completeStep(trekId: Long) = client.post(Api.Journey.CompleteStep, trekId)
    suspend fun startTrek(trekId: Long) = client.post(Api.Journey.StartTrek, trekId)
    suspend fun pauseTrek(trekId: Long) = client.post(Api.Journey.PauseTrek, trekId)
    suspend fun stepIntoPath(trekId: Long) = client.post(Api.Journey.StepIntoPath, trekId)
    suspend fun readFocus() = client.getOrNull(Api.Journey.FocusTrek)
}
