package ponder.steps

import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import ponder.steps.db.AppDatabase
import ponder.steps.io.DataMerger
import ponder.steps.io.LocalSyncRepository
import ponder.steps.io.RemoteSyncRepository
import pondui.LocalValueRepository
import pondui.ProvideWavePlayer

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
                val scope = rememberCoroutineScope()
                remember {
                    val sync = DataMerger(
                        localRepo = LocalSyncRepository(),
                        remoteRepo = RemoteSyncRepository(),
                    )
                    sync.init(scope)
                }

                PondApp(
                    config = appConfig,
                    changeRoute = changeRoute,
                    exitApp = exitApp
                )
            }
        }
    }
}

var _db: AppDatabase? = null
val appDb: AppDatabase get() = _db ?: error("You must initialize the database")
val appOrigin = getOrigin()
val appUserId = "wombat7"

expect fun getOrigin(): String