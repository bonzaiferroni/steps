package ponder.steps.ui

import ponder.steps.model.data.Trek
import pondui.ui.core.StateModel

class TrekListModel: StateModel<TrekListState>(TrekListState()) {
}

data class TrekListState(
    val treks: List<Trek> = emptyList()
)