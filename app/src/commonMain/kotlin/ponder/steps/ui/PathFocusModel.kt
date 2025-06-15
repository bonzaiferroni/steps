package ponder.steps.ui

import ponder.steps.model.data.Intent
import ponder.steps.model.data.LogEntry
import ponder.steps.model.data.Step
import ponder.steps.model.data.Trek
import pondui.ui.core.StateModel

class PathFocusModel: StateModel<PathFocusState>(PathFocusState()) {


}

data class PathFocusState(
    val trek: Trek? = null,
    val intent: Intent? = null,
    val step: Step? = null,
    val subSteps: List<Step> = emptyList(),
    val subTreks: List<Trek> = emptyList(),
    val logs: List<LogEntry> = emptyList(),
)