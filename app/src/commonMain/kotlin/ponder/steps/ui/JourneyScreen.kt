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
            Tab("Focus") { FocusView() }
            Tab("Treks") { TrekListView() }
            Tab("Plans") { IntentListView() }
        }
    }
}