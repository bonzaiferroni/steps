package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.LocalIntentRepository
import ponder.steps.io.LocalStepRepository
import ponder.steps.model.data.Intent
import ponder.steps.model.data.NewIntent
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel

class IntentListModel(
    private val intentRepo: LocalIntentRepository = LocalIntentRepository(),
    private val stepRepo: LocalStepRepository = LocalStepRepository()
) : StateModel<IntentionState>(IntentionState()) {

    init {
        intentRepo.readActiveIntentsFlow().launchCollect { intents ->
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
            val steps = stepRepo.readSearch(query)
            setState { it.copy(searchPaths = steps) }
        }
    }

    fun createIntent(step: Step) {
        viewModelScope.launch {
            intentRepo.createIntent(NewIntent(
                rootId = step.id,
                label = step.label,
                expectedMins = step.expectedMins,
                repeatMins = 60
            ))
            setState { it.copy(isAddingItem = false) }
        }
    }

    fun removeIntent(intent: Intent) {
        viewModelScope.launch {
            intentRepo.completeIntent(intent.id)
        }
    }
}

data class IntentionState(
    val intents: List<Intent> = emptyList(),
    val isAddingItem: Boolean = false,
    val searchPathText: String = "",
    val searchPaths: List<Step> = emptyList()
)
