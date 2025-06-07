package ponder.steps.ui

import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentListOf
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Tab
import pondui.ui.controls.Tabs

@Composable
fun JourneyScreen() {
    Scaffold {
        Tabs {
            Tab("Doing") { TodoView() }
            Tab("Focus") { FocusView() }
            Tab("Plan") { IntentListView() }
        }
    }
}