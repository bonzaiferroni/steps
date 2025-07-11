package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.appDb
import ponder.steps.db.TrekPoint
import ponder.steps.db.TrekPointDao
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.LocalTrekRepository
import ponder.steps.io.SyncAgent
import ponder.steps.model.data.Intent
import pondui.ui.core.SubModel
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class TrekStarter(
    viewModel: ViewModel,
    private val intentRepo: LocalIntentRepository = LocalIntentRepository(),
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
    private val trekPointDao: TrekPointDao = appDb.getTrekPointDao()
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
                    delay(100)
                }

                println("checking treks")
                val now = Clock.System.now()
                nextRefresh = now + 1.hours

                val activeIntents = mutableListOf<Intent>()

                val ids = intents.map { it.id }
                val trekPoints = trekPointDao.readActiveTrekPoints(ids)

                for (intent in intents) {
                    val scheduledAt = intent.scheduledAt
                    val trekPoint = trekPoints.firstOrNull { it.intentId == intent.id }
                    var createTrek = trekPoint == null

                    if (scheduledAt != null && scheduledAt > now) {
                        nextRefresh = minOf(nextRefresh, scheduledAt)
                        continue
                    }

                    val repeatMins = intent.repeatMins
                    if (repeatMins != null) {
                        val finishedAt = trekPoint?.finishedAt
                        val repeatTime = finishedAt?.let { it + repeatMins.minutes } ?: (now + repeatMins.minutes)
                        if (repeatTime > now) {
                            nextRefresh = minOf(nextRefresh, repeatTime)
                            continue
                        } else {
                            createTrek = true
                        }
                    }

                    if (createTrek && isActive) {
                        println("creating trek point for intent: ${intent.label}")
                        // trekRepo.createTrekFromIntent(intent)
                        trekPointDao.createTrekPoint(TrekPoint(intentId = intent.id))
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