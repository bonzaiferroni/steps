package ponder.steps.ui

import androidx.compose.runtime.Composable
import ponder.steps.PathEditorRoute
import pondui.ui.controls.Scaffold

@Composable
fun PathEditorScreen(
    route: PathEditorRoute
) {
    Scaffold {
        PathEditorView(route.pathId)
    }
}