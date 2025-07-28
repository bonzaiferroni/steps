package ponder.steps.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.clients.GeminiRole
import pondui.ui.behavior.onEnterPressed
import pondui.ui.controls.*
import pondui.ui.controls.Scaffold
import pondui.ui.theme.Pond

/**
 * Ahoy! This be the Gemini screen, where the magic of AI be happenin'!
 * Here ye can chat with the AI and get yer answers from the digital oracle!
 */
@Composable
fun GeminiScreen() {
    val viewModel: GeminiModel = viewModel { GeminiModel() }
    val state by viewModel.stateFlow.collectAsState()

    Scaffold {
        Column(1) {
            // Messages display area
            LazyColumn(1, modifier = Modifier.weight(1f)) {
                items(state.messages) { message ->
                    val isUser = message.role == GeminiRole.User
                    if (isUser) {
                        Text(
                            "You: ${message.message}", color = Color.LightGray
                        )
                    } else {
                        Text(
                            "AI: ${message.message}",
                        )
                    }
                }
            }

            // Show loading indicator when waiting for response
            if (state.isLoading) {
                Text("The AI be thinkin'...", color = Pond.colors.action)
            }

            // Input area at the bottom
            ControlSet {
                TextField(
                    state.message,
                    onTextChanged = viewModel::updateMessage,
                    modifier = Modifier.weight(1f)
                        .onEnterPressed(viewModel::sendMessage)
                )

                ControlSetButton(
                    "Send",
                    onClick = viewModel::sendMessage,
                    isEnabled = state.message.isNotEmpty() && !state.isLoading
                )
            }
        }
    }
}
