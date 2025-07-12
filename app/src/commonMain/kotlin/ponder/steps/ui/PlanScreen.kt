package ponder.steps.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Trash
import pondui.ui.controls.*
import pondui.ui.theme.Pond

@Composable
fun PlanScreen() {
    val viewModel = viewModel { IntentListModel() }
    val state by viewModel.state.collectAsState()

    AddStepCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        createIntent = true,
        pathId = null,
        dismiss = viewModel::toggleAddItem
    )

    Column(1) {

        TopBarSpacer()

        LazyColumn(1, modifier = Modifier.weight(1f)) {
            items(state.intents, key = { it.id }) { item ->
                Row(1) {
                    Text(item.label)
                    Expando()
                    Button(TablerIcons.Trash, Pond.colors.danger) { viewModel.removeIntent(item) }
                }
            }
        }
        Button("Add", onClick = viewModel::toggleAddItem)

        BottomBarSpacer()
    }
}