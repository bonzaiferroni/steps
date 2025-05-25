package ponder.steps.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.clients.GeminiMessage
import kabinet.clients.GeminiRole
import pondui.ui.controls.*
import pondui.ui.nav.Scaffold
import pondui.ui.theme.Pond
import pondui.ui.theme.Spacing

/**
 * Ahoy! This be the Gemini screen, where the magic of AI be happenin'!
 * Here ye can chat with the AI and get yer answers from the digital oracle!
 */
@Composable
fun GeminiScreen() {
    val viewModel: GeminiModel = viewModel { GeminiModel() }
    val state by viewModel.state.collectAsState()

    Scaffold {
        Column(Spacing.Unit) {
            // Messages display area
            LazyColumn(Spacing.Unit) {
                items(state.messages) { message ->
                    val isUser = message.role == GeminiRole.User
                    if (isUser) {
                        Text(
                            "You: ${message.message}",
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
                Text("The AI be thinkin'...", color = Pond.colors.secondary)
            }

            // Input area at the bottom
            ControlSet {
                TextField(
                    state.message,
                    onTextChange = viewModel::updateMessage,
                )

                Button(
                    "Send",
                    onClick = viewModel::sendMessage,
                    isEnabled = state.message.isNotEmpty() && !state.isLoading
                )
            }
        }
    }
}
