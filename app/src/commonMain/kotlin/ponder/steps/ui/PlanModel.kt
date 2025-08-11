package ponder.steps.ui

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ponder.steps.io.LocalIntentRepository
import ponder.steps.model.data.Intent
import ponder.steps.model.data.IntentId
import ponder.steps.model.data.Step
import pondui.ui.core.StateModel
import pondui.ui.core.ModelState

class PlanModel(
    private val intentRepo: LocalIntentRepository = LocalIntentRepository(),
) : StateModel<PlanState>() {
    override val state = ModelState(PlanState())

    init {
        intentRepo.flowActiveIntents().launchCollect { intents ->
            setState { it.copy(intents = intents) }
        }
    }

    fun toggleAddItem() {
        setState { it.copy(isAddingItem = !it.isAddingItem) }
    }

    fun removeIntent(intent: Intent) {
        viewModelScope.launch {
            intentRepo.completeIntent(intent.id)
        }
    }

    fun setEditIntentId(value: IntentId?) {
        setState { it.copy(editIntentId = value) }
    }
}

data class PlanState(
    val intents: List<Intent> = emptyList(),
    val isAddingItem: Boolean = false,
    val searchPathText: String = "",
    val searchPaths: List<Step> = emptyList(),
    val editIntentId: IntentId? = null,
)
