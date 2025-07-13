package ponder.steps.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Edit
import compose.icons.tablericons.Trash
import kabinet.utils.toShortDescription
import kotlinx.datetime.Clock
import pondui.ui.controls.*
import pondui.ui.theme.Pond
import kotlin.time.Duration.Companion.minutes

@Composable
fun PlanScreen() {
    val viewModel = viewModel { PlanModel() }
    val state by viewModel.state.collectAsState()

    AddStepCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        createIntent = true,
        pathId = null,
        dismiss = viewModel::toggleAddItem
    )

    EditIntentCloud(
        intentId = state.editIntentId,
        isVisible = state.editIntentId != null,
        dismiss = { viewModel.setEditIntentId(null) }
    )

    Column(1) {

        TopBarSpacer()

        LazyColumn(1, modifier = Modifier.weight(1f)) {
            items(state.intents, key = { it.id }) { intent ->
                Row(1) {
                    FlowRow(1, modifier = Modifier.weight(1f)) {
                        Text(intent.label)
                        intent.repeatMins?.let {
                            Label("repeats every ${it.minutes.toShortDescription()}")
                        }
                        intent.scheduledAt?.let {
                            val now = Clock.System.now()
                            if (it > now) Label("scheduled for ${(it - now).toShortDescription()}")
                        }
                    }
                    Button(TablerIcons.Edit, Pond.colors.secondary) { viewModel.setEditIntentId(intent.id) }
                    Button(TablerIcons.Trash, Pond.colors.danger) { viewModel.removeIntent(intent) }
                }
            }
        }
        Button("Add", onClick = viewModel::toggleAddItem)

        BottomBarSpacer()
    }
}