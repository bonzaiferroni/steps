package ponder.steps.io

import ponder.steps.model.Api
import pondui.io.ApiStore

class JourneyStore: ApiStore() {
    suspend fun readUserTreks() = client.get(Api.Journey.UserTreks)
    suspend fun completeStep(trekId: Long) = client.post(Api.Journey.CompleteStep, trekId)
    suspend fun startTrek(trekId: Long) = client.post(Api.Journey.StartTrek, trekId)
}