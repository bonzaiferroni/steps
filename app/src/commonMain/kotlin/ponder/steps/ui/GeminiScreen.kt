package ponder.steps.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kabinet.clients.GeminiClient
import pondui.ui.controls.Text
import pondui.ui.nav.Scaffold

/**
 * Ahoy! This be the Gemini screen, where the magic of AI be happenin'!
 * For now, it be as empty as a deserted island, but soon it'll be filled with treasures!
 */
@Composable
fun GeminiScreen(
    viewModel: GeminiModel = viewModel { 
        // Create a simple GeminiClient with a placeholder token
        // Ye'll need to replace this with a real token when ye set sail for real!
        val geminiClient = GeminiClient(
            limitedToken = "ye_need_a_real_token_here_matey",
            logMessage = { source, level, message -> 
                println("[$source] ${level.name}: $message") 
            }
        )
        GeminiModel(geminiClient) 
    }
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold {
        // Arr! This be a simple screen with just a text for now
        // We'll be addin' more booty in the next voyage!
        Text(state.placeholder)
    }
}