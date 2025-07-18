package ponder.steps

import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import ponder.steps.db.AppDatabase
import ponder.steps.io.ProvideImageSource
import ponder.steps.io.SyncAgent
import ponder.steps.ui.TrekStarter
import pondui.ProvideWavePlayer
import pondui.io.LocalUserContext

import pondui.io.ProvideUserContext
import pondui.ui.core.PondApp
import pondui.ui.nav.NavRoute
import pondui.ui.theme.ProvideTheme

@Composable
@Preview
fun App(
    changeRoute: (NavRoute) -> Unit,
    exitApp: (() -> Unit)?,
) {
    ProvideTheme {
        ProvideWavePlayer {
            ProvideUserContext {
                val userContext = LocalUserContext.current
                if (userContext != null) {
                    val state by userContext.state.collectAsState()
                    LaunchedEffect(state.isLoggedIn) {
                        val syncAgent = SyncAgent(appOrigin, appDb)
                        if (state.isLoggedIn) {
                            syncAgent.startSync()
                        }
                    }
                }
                viewModel { TrekStarter() }

                ProvideImageSource {
                    PondApp(
                        config = appConfig,
                        changeRoute = changeRoute,
                        exitApp = exitApp
                    )
                }
            }
        }
    }
}

var _db: AppDatabase? = null
val appDb: AppDatabase get() = _db ?: error("You must initialize the database")
val appOrigin = getOrigin()
val appUserId = "wombat7"

expect fun getOrigin(): String