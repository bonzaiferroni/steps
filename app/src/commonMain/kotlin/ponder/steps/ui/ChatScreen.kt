package ponder.steps.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.behavior.onEnterPressed
import pondui.ui.controls.Button
import pondui.ui.controls.Label
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField

@Composable
fun ChatScreen() {
    val viewModel = viewModel { ChatModel() }
    val state by viewModel.state.collectAsState()

    Scaffold {
        LazyColumn(1, modifier = Modifier.weight(1f)) {
            items(state.messages) { message ->
                Row(1) {
                    Label("${message.origin}:")
                    Text(message.content)
                }
            }
        }

        Row(1) {
            TextField(state.message, viewModel::setMessage, modifier = Modifier.weight(1f)
                .onEnterPressed(viewModel::sendMessage))
            Button("Send", onClick = viewModel::sendMessage)
        }
    }
}