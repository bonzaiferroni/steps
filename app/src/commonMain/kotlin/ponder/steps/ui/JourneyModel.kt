package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.JourneyStore
import ponder.steps.model.data.TrekItem
import pondui.ui.core.StateModel

class JourneyModel: StateModel<JourneyState>(JourneyState()) {
    private val journeyStore = JourneyStore()

    init {
        refreshItems()
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
}

data class JourneyState(
    val trekItems: List<TrekItem> = emptyList()
)
