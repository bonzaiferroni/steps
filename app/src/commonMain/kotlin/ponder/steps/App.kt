package ponder.steps

import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import ponder.steps.db.AppDatabase
import ponder.steps.io.DataSync
import ponder.steps.io.StepLocalRepository
import ponder.steps.io.StepServerRepository
import pondui.KeyStore
import pondui.io.LocalUserContext

import pondui.io.ProvideUserContext
import pondui.io.collectState
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
        ProvideUserContext {
            val keyStore = remember {
                val keyStore = KeyStore()
                _appUserId = keyStore.readStringOrNull("userId") ?: _appUserId
                keyStore
            }
            val scope = rememberCoroutineScope()
            remember {
                val sync = DataSync(
                    leftRepo = StepLocalRepository(),
                    rightRepo = StepServerRepository(),
                    lastSyncAt = keyStore.readInstant("lastUpdatedAt"),
                    onSync = { keyStore.writeInstant("lastUpdatedAt", it) }
                )
                sync.init(scope)
            }
            val userContextState by LocalUserContext.collectState()
            LaunchedEffect(userContextState) {
                userContextState.user?.let {
                    _appUserId = it.id
                    keyStore.writeString("userId", it.id)
                }
            }

            PondApp(
                config = appConfig,
                changeRoute = changeRoute,
                exitApp = exitApp
            )
        }
    }
}

var _db: AppDatabase? = null
val appDb: AppDatabase get() = _db ?: error("You must initialize the database")

var _appUserId = "wombat7"
val appUserId get() = _appUserId