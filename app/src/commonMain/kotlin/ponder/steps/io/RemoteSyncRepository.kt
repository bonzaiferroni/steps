package ponder.steps.io

import kotlinx.datetime.Instant
import ponder.steps.model.Api
import ponder.steps.model.data.ReadSyncRequest
import ponder.steps.model.data.SyncData
import pondui.io.ApiClient
import pondui.io.globalApiClient

class RemoteSyncRepository(
    private val client: ApiClient = globalApiClient
) : SyncRepository {

    override suspend fun readSync(startSyncAt: Instant, endSyncAt: Instant) = client.post(
        Api.Sync.Read,
        ReadSyncRequest(startSyncAt, endSyncAt)
    )

    override suspend fun writeSync(data: SyncData) = client.post(Api.Sync.Write, data)
}