package ponder.steps.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
        loadIntents()
    }

    private fun loadIntents() {
        viewModelScope.launch {
            try {
                val intents = intentionStore.readUserIntents()
                setState { it.copy(intents = intents) }
            } catch (e: Exception) {
                // Handle error
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
            try {
                // This is a placeholder. You'll need to implement a search endpoint in the StepStore
                // For now, we'll just load root steps as an example
                val steps = stepStore.readRootSteps(includeChildren = false)
                val filteredSteps = steps.filter { it.label.contains(query, ignoreCase = true) }
                setState { it.copy(searchPaths = filteredSteps) }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun createIntent(rootId: Long) {
        viewModelScope.launch {
            try {
                val step = stepStore.readStep(stepId = rootId, includeChildren = false)
                val newIntent = NewIntent(
                    rootId = rootId,
                    label = step.label,
                    expectedMins = step.expectedMins
                )
                val intentId = intentionStore.createIntent(newIntent)
                loadIntents() // Reload intents after creating a new one
                setState { it.copy(isAddingItem = false) } // Close the add item panel
            } catch (e: Exception) {
                // Handle error
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
