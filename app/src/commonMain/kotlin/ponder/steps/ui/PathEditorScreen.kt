package ponder.steps.ui

import androidx.compose.runtime.Composable
import ponder.steps.PathEditorRoute

@Composable
fun PathEditorScreen(
    route: PathEditorRoute
) {
    PathEditorView(route.pathId)
}