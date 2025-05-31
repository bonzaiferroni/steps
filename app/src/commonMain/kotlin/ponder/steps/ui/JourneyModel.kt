package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ponder.steps.io.JourneyStore
import ponder.steps.model.data.TrekItem
import pondui.ui.core.StateModel
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class JourneyModel: StateModel<JourneyState>(JourneyState()) {
    private val journeyStore = JourneyStore()

    init {
        refreshItems()
        refreshUi()
    }

    private fun refreshUi() {
        viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                setState { it.copy(refreshedUiAt = Clock.System.now()) }
            }
        }
    }

    fun refreshItems() {
        viewModelScope.launch {
            val trekItems = journeyStore.readUserTreks().sortedBy { it.availableAt }
            setState { it.copy(trekItems = trekItems) }
        }
    }

    fun completeStep(item: TrekItem) {
        viewModelScope.launch {
            val success = journeyStore.completeStep(item.trekId)
            if (success == true) {
                refreshItems()
            }
        }
    }

    fun startTrek(item: TrekItem) {
        viewModelScope.launch {
            val success = journeyStore.startTrek(item.trekId)
            if (success == true) {
                refreshItems()
            }
        }
    }

    fun pauseTrek(item: TrekItem) {
        viewModelScope.launch {
            val success = journeyStore.pauseTrek(item.trekId)
            if (success == true) {
                refreshItems()
            }
        }
    }

    fun stepIntoPath(item: TrekItem) {

    }
}

data class JourneyState(
    val trekItems: List<TrekItem> = emptyList(),
    val refreshedUiAt: Instant = Clock.System.now(),
)
