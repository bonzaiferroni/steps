package ponder.steps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import kotlinx.collections.immutable.toImmutableList
import ponder.steps.model.data.Answer
import ponder.steps.model.data.Question
import pondui.ui.behavior.MagicItem
import pondui.ui.behavior.magic
import pondui.ui.controls.BottomBarSpacer
import pondui.ui.controls.Button
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.MenuWheel
import pondui.ui.nav.LocalNav

@Composable
fun TodoView() {
    val viewModel = viewModel { TodoModel() }
    val state by viewModel.state.collectAsState()
    val nav = LocalNav.current

    DisposableEffect(Unit) {
        onDispose(viewModel::onDispose)
    }

    LaunchedEffect(Unit) {
        viewModel.onLoad()
    }

    AddIntentCloud(
        title = "Add step",
        isVisible = state.isAddingItem,
        dismiss = viewModel::toggleAddItem
    )

    LazyColumn(1, horizontalAlignment = Alignment.CenterHorizontally) {
        item(key = "span") {
            MenuWheel(state.span, TrekSpan.entries.toImmutableList()) { viewModel::setSpan }
        }
        items(state.items, key = { it.trekId }) { item ->
            val questionSet = state.questionSets.firstOrNull { it.trekId == item.trekId }
            val question = questionSet?.questions?.firstOrNull()
            MagicItem(
                item = question,
                itemContent = { question ->
                    QuestionRow(question) { viewModel.answerQuestion(item.trekId, question, it) }
                },
                modifier = Modifier.animateItem()
            ) {
                TrekItemRow(item, viewModel::completeStep)
            }
        }
        item(key = "add button") {
            Button(TablerIcons.Plus, onClick = viewModel::toggleAddItem)
        }
        item(key = "bottom spacer") {
            BottomBarSpacer()
        }
    }
}