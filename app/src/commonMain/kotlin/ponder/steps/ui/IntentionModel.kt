package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.IntentionStore
import ponder.steps.io.StepStore
import ponder.steps.model.data.Intent
import ponder.steps.model.data.NewIntent
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel

class IntentionModel: StateModel<IntentionState>(IntentionState()) {
    private val intentionStore = IntentionStore()
    private val stepStore = StepStore()

    init {
        refreshItems()
    }

    fun refreshItems() {
        viewModelScope.launch {
            val intents = intentionStore.readUserIntents()
            setState { it.copy(intents = intents) }
        }
    }

    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun setSearchPathText(text: String) {
        setState { it.copy(searchPathText = text) }
        if (text.isNotEmpty()) {
            searchPaths(text)
        } else {
            setState { it.copy(searchPaths = emptyList()) }
        }
    }

    private fun searchPaths(query: String) {
        viewModelScope.launch {
            val steps = stepStore.searchSteps(query, includeChildren = false)
            setState { it.copy(searchPaths = steps) }
        }
    }

    fun createIntent(step: Step) {
        viewModelScope.launch {
            val newIntent = NewIntent(
                rootId = step.id,
                label = step.label,
                expectedMins = step.expectedMins
            )
            val intentId = intentionStore.createIntent(newIntent)
            if (intentId != null) {
                refreshItems() // Reload intents after creating a new one
                setState { it.copy(isAddingItem = false) } // Close the add item panel
            }
        }
    }
}

data class IntentionState(
    val intents: List<Intent> = emptyList(),
    val isAddingItem: Boolean = false,
    val searchPathText: String = "",
    val searchPaths: List<Step> = emptyList()
)
