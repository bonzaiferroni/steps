package ponder.steps.io

import kotlinx.datetime.Instant
import ponder.steps.model.Api
import ponder.steps.model.data.SyncData
import pondui.io.ApiClient
import pondui.io.globalApiClient

class RemoteSyncRepository(
    private val client: ApiClient = globalApiClient
): SyncRepository {

    override suspend fun readSync(lastSyncAt: Instant) = client.post(Api.Steps.ReadSync, lastSyncAt)

    override suspend fun writeSync(data: SyncData) = client.post(Api.Steps.WriteSync, data)
}