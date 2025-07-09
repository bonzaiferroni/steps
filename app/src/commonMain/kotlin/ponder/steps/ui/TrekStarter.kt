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
import kotlinx.datetime.Instant
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.SyncAgent
import ponder.steps.model.data.Intent
import pondui.ui.core.SubModel
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class TrekStarter(
    viewModel: ViewModel,
    private val intentRepo: LocalIntentRepository = LocalIntentRepository(),
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
) : SubModel<TrekStarterState>(TrekStarterState(), viewModel) {

    private var intents: List<Intent> = emptyList()
    private var nextRefresh = Instant.DISTANT_PAST

    init {
        intentRepo.flowActiveIntents().launchCollect { intents -> refreshIntents(intents) }
        startTreks()
    }

    private fun refreshIntents(intents: List<Intent>) {
        this.intents = intents
        nextRefresh = Clock.System.now()
    }

    private fun startTreks() {
        viewModelScope.launch {
            while (isActive) {
                while (isActive && nextRefresh > Clock.System.now() || SyncAgent.syncInProgress) {
                    delay(1000)
                }

                println("checking treks")
                val start = Clock.startOfDay()
                val now = Clock.System.now()
                nextRefresh = now + 1.hours

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
            }
        }
    }
}

@Stable
data class TrekStarterState(
    val intents: List<Intent> = emptyList()
)