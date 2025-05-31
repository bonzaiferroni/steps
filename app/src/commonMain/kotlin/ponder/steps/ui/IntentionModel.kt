package ponder.steps.ui

import ponder.steps.model.data.Intent
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel

class IntentionModel: StateModel<IntentionState>(IntentionState()) {
    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun setSearchPathText(text: String) {
        setState { it.copy(searchPathText = text) }
    }

    fun createIntent(rootId: Long) {

    }


}

data class IntentionState(
    val intents: List<Intent> = emptyList(),
    val isAddingItem: Boolean = false,
    val searchPathText: String = "",
    val searchPaths: List<Step> = emptyList()
)