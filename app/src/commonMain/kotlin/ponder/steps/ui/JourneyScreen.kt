package ponder.steps.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentListOf
import pondui.ui.controls.Column
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Tab
import pondui.ui.controls.Tabs
import pondui.ui.controls.TopBarSpacer

@Composable
fun JourneyScreen() {
    Column(1) {
        TopBarSpacer()

        Tabs {
            Tab("Doing") { TodoView() }
            Tab("Focus") { FocusView() }
            Tab("Plan") { IntentListView() }
        }
    }
}