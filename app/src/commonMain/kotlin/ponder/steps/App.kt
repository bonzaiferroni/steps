package ponder.steps

import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import ponder.steps.db.AppDatabase
import ponder.steps.io.DataMerger
import ponder.steps.io.LocalStepRepository
import ponder.steps.io.StepServerRepository
import pondui.LocalValueRepository
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
                val settingsValueRepository = LocalValueRepository()
                _appUserId = settingsValueRepository.readStringOrNull("userId") ?: _appUserId
                settingsValueRepository
            }
            val scope = rememberCoroutineScope()
            remember {
                val sync = DataMerger(
                    leftRepo = LocalStepRepository(),
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