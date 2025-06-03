package ponder.steps.ui

import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentListOf
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Tab
import pondui.ui.controls.Tabs

@Composable
fun JourneyScreen() {
    Scaffold {
        Tabs(
            tabs = persistentListOf(
                Tab("Focus") { FocusView() },
                Tab("Treks", scrollable = false) { TrekListView() },
                Tab("Plans", scrollable = false) { IntentListView() },
            )
        )
    }
}