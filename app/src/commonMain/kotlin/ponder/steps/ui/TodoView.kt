package ponder.steps.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Plus
import ponder.steps.model.data.StepOutcome
import ponder.steps.model.data.TrekStep
import pondui.ui.behavior.MagicItem
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.H1
import pondui.ui.controls.IconButton
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.ProgressBar
import pondui.ui.controls.Row
import pondui.ui.controls.Section
import pondui.ui.controls.Text
import pondui.ui.theme.Pond

@Composable
fun TodoView() {
    val viewModel = viewModel { TodoModel() }
    val state by viewModel.state.collectAsState()

    MagicItem(
        item = state.trekId,
        offsetX = if (state.trekId != null) 30.dp else (-30).dp,
    ) { trekId ->
        if (trekId != null) {
            TrekProfileView(trekId, viewModel::loadTrek)
        } else {
            TodoRootView(viewModel::loadTrek)
        }
    }
}