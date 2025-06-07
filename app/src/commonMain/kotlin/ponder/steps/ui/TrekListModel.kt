package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kabinet.utils.startOfDay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.io.LocalTrekRepository
import ponder.steps.model.data.TrekItem
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TrekListModel(
    private val localTrekRepository: LocalTrekRepository = LocalTrekRepository()
): StateModel<JourneyState>(JourneyState()) {

    fun onLoad() {
        viewModelScope.launch {
            localTrekRepository.flowTreksSince(Clock.startOfDay()).collect { treks ->
                setState { it.copy(treks = treks) }
            }
        }
        viewModelScope.launch {
            while (true) {
                localTrekRepository.syncTreksWithIntents()
                delay(1.minutes)
            }
        }
        viewModelScope.launch {
            while (true) {
                setState { it.copy(refreshedUiAt = Clock.System.now()) }
                delay(1.seconds)
            }
        }
    }

    fun onDispose() = cancelJobs()

    fun completeStep(item: TrekItem) {
        viewModelScope.launch {
            localTrekRepository.completeStep(item.trekId)
        }
    }

    fun startTrek(item: TrekItem) {
        viewModelScope.launch {
            localTrekRepository.startTrek(item.trekId)
        }
    }

    fun pauseTrek(item: TrekItem) {
        viewModelScope.launch {
            localTrekRepository.pauseTrek(item.trekId)
        }
    }

    fun stepIntoPath(item: TrekItem) {
        viewModelScope.launch {
            localTrekRepository.stepIntoPath(item.trekId)
        }
    }
}

data class JourneyState(
    val treks: List<TrekItem> = emptyList(),
    val refreshedUiAt: Instant = Clock.System.now(),
)
