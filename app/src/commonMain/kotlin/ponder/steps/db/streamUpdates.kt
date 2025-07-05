package ponder.steps.db

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

fun <T> AppDatabase.streamUpdates(
    tableName: String,
    lastSync: Instant,
    readUpdates: suspend (Instant) -> List<T>,
    provideUpdatedAt: (T) -> Instant,
): Flow<List<T>> = channelFlow {
    var lastSeen = lastSync
    // this Flow emits a Set<String> every time "YourTable" changes
    val tableChanges = invalidationTracker.createFlow(tableName, emitInitialState = false)
    launch {
        tableChanges.collect {
            // query only the fresh rows since lastSeen
            val newOnes = readUpdates(lastSeen)
            if (newOnes.isNotEmpty()) {
                send(newOnes)
                // bump the watermark
                lastSeen = maxOf(lastSeen, newOnes.maxOfOrNull { provideUpdatedAt(it) } ?: lastSeen)
            }
        }
    }
    awaitClose { /* nothing to tear down */ }
}