package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.io.TrekStore
import ponder.steps.model.data.TrekItem
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class JourneyModel(
    private val trekStore: TrekStore = TrekStore()
): StateModel<JourneyState>(JourneyState()) {

    init {
        viewModelScope.launch {
            trekStore.readTreksSince(Clock.startOfDay()).collect { treks ->
                setState { it.copy(treks = treks) }
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                setState { it.copy(refreshedUiAt = Clock.System.now()) }
            }
        }
        viewModelScope.launch {
            while (true) {
                trekStore.syncTreksWithIntents()
                delay(1.minutes)
            }
        }
    }

    fun completeStep(item: TrekItem) {
        viewModelScope.launch {
            trekStore.completeStep(item.trekId)
        }
    }

    fun startTrek(item: TrekItem) {
        viewModelScope.launch {
            trekStore.startTrek(item.trekId)
        }
    }

    fun pauseTrek(item: TrekItem) {
        viewModelScope.launch {
            trekStore.pauseTrek(item.trekId)
        }
    }

    fun stepIntoPath(item: TrekItem) {
        viewModelScope.launch {
            trekStore.stepIntoPath(item.trekId)
        }
    }
}

data class JourneyState(
    val treks: List<TrekItem> = emptyList(),
    val refreshedUiAt: Instant = Clock.System.now(),
)
