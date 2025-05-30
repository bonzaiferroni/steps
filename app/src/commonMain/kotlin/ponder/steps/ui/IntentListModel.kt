package ponder.steps.ui

import ponder.steps.model.data.Intent
import pondui.ui.core.StateModel

class IntentListModel: StateModel<IntentListState>(IntentListState()) {
}

data class IntentListState(
    val intents: List<Intent> = emptyList()
)