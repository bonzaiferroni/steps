package ponder.steps.ui

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
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
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class TrekStarter(
    private val intentRepo: LocalIntentRepository = LocalIntentRepository(),
    private val trekRepo: LocalTrekRepository = LocalTrekRepository(),
    private val trekPointDao: TrekPointDao = appDb.getTrekPointDao()
): StateModel<TrekStarterState>() {

    override val state = ModelState(TrekStarterState())

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
        println("starting trekStarter")
        viewModelScope.launch {
            while (isActive) {
                while (isActive && nextRefresh > Clock.System.now() || SyncAgent.syncInProgress) {
                    delay(100)
                }

                val now = Clock.System.now()
                println("checking treks: $now")
                nextRefresh = now + 1.hours
                val currentIntents = intents

                val activeIntents = mutableListOf<Intent>()

                val ids = currentIntents.map { it.id }
                val trekPoints = trekPointDao.readLastTrekPoints(ids)

                for (intent in currentIntents) {
                    val trekPoint = trekPoints.firstOrNull { it.intentId == intent.id }

                    val repeatAt = intent.repeatMins?.let { trekPoint?.finishedAt?.plus(it.minutes) }
                    val activeAt = intent.scheduledAt ?: repeatAt ?: now

                    val nextPossibleAt = intent.scheduledAt ?: repeatAt ?: intent.repeatMins?.let { now.plus(it.minutes) }
                    if (nextPossibleAt != null && nextPossibleAt > now) {
                        nextRefresh = minOf(nextRefresh, nextPossibleAt)
                    }

                    if (activeAt > now) {
                        println("ay not active yet")
                        continue
                    }

                    val repeatDue = repeatAt != null && now > repeatAt
                    if ((trekPoint == null || repeatDue) && isActive) {
                        println("creating trek point for intent: ${intent.label}")
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