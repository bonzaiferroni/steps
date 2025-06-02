package ponder.steps

import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import ponder.steps.db.AppDatabase

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
        ProvideUserContext {
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

val appUserId = "wombat7"