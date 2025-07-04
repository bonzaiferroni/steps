package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kabinet.utils.startOfDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ponder.steps.io.DataMerger
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.model.data.Intent
import ponder.steps.model.data.Trek
import pondui.ui.core.SubModel
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class TrekStarter(
    viewModel: ViewModel,
    private val intentRepo: LocalIntentRepository = LocalIntentRepository(),
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
) : SubModel<TrekStarterState>(TrekStarterState(), viewModel) {

    init {
        intentRepo.readActiveIntentsFlow().launchCollect { intents -> refreshIntents(intents) }
    }

    private var refreshJob: Job? = null

    private fun refreshIntents(intents: List<Intent>) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                while (DataMerger.syncInProgress) {
                    println("delaying trekStarter for sync")
                    delay(1000)
                }
                val start = Clock.startOfDay()
                val end = start + 1.days
                val now = Clock.System.now()
                var nextRefresh = end

                val activeIntents = mutableListOf<Intent>()

                val ids = intents.map { it.id }
                val treks = trekRepo.readTreksLastStartedAt(ids)

                for (intent in intents) {
                    val scheduledAt = intent.scheduledAt
                    val trek = treks.firstOrNull { it.intentId == intent.id }
                    var createTrek = trek == null

                    if (scheduledAt != null && scheduledAt > now) {
                        nextRefresh = minOf(nextRefresh, scheduledAt)
                    }

                    val repeatMins = intent.repeatMins
                    if (repeatMins != null) {
                        val finishedAt = trek?.finishedAt
                        val repeatTime = finishedAt?.let { it + repeatMins.minutes } ?: (now + repeatMins.minutes)
                        if (repeatTime > now) {
                            nextRefresh = minOf(nextRefresh, repeatTime)
                        } else {
                            createTrek = true
                        }
                    }

                    if (createTrek && isActive) {
                        println("creating trek for intent: ${intent.label}")
                        trekRepo.createTrekFromIntent(intent)
                    }
                    activeIntents.add(intent)
                }

                setState { it.copy(intents = activeIntents) }

                delay(nextRefresh - Clock.System.now())
            }
        }
    }
}

@Stable
data class TrekStarterState(
    val intents: List<Intent> = emptyList()
)