package ponder.steps.ui

import androidx.compose.runtime.Composable
import pondui.ui.controls.Column
import pondui.ui.controls.Tab
import pondui.ui.controls.Tabs
import pondui.ui.controls.TopBarSpacer

@Composable
fun JourneyScreen() {
    Column(1) {
        TopBarSpacer()

        Tabs {
            Tab("Today") { TodoScreen() }
            // Tab("Doing") { TodoOldView() }
            Tab("Focus") { FocusView() }
            Tab("Plan") { PlanScreen() }
        }
    }
}