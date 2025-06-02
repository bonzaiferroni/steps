package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.IntentStore
import ponder.steps.io.StepStore
import ponder.steps.model.data.Intent
import ponder.steps.model.data.NewIntent
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel

class IntentionModel(
    private val intentStore: IntentStore = IntentStore(),
    private val stepStore: StepStore = StepStore()
) : StateModel<IntentionState>(IntentionState()) {

    init {
        viewModelScope.launch {
            intentStore.readActiveIntentsFlow().collect { intents ->
                setState { it.copy(intents = intents) }
            }
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
            val steps = stepStore.searchSteps(query)
            setState { it.copy(searchPaths = steps) }
        }
    }

    fun createIntent(step: Step) {
        viewModelScope.launch {
            intentStore.createIntent(NewIntent(
                rootId = step.id,
                label = step.label,
                expectedMins = step.expectedMins,
                repeatMins = 60
            ))
            setState { it.copy(isAddingItem = false) }
        }
    }
}

data class IntentionState(
    val intents: List<Intent> = emptyList(),
    val isAddingItem: Boolean = false,
    val searchPathText: String = "",
    val searchPaths: List<Step> = emptyList()
)
