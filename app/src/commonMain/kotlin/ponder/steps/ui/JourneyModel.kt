package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.model.data.TrekItem
import pondui.ui.core.StateModel

class JourneyModel: StateModel<JourneyState>(JourneyState()) {

    init {
        refreshItems()
    }

    fun refreshItems() {
    }

    fun completeStep(item: TrekItem) {
    }

    fun startTrek(item: TrekItem) {
    }
}

data class JourneyState(
    val trekItems: List<TrekItem> = emptyList()
)