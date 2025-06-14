package ponder.steps

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ponder.steps.db.getDatabaseBuilder
import ponder.steps.db.getRoomDatabase
import pondui.ui.controls.AppWindow
import pondui.CacheFile
import pondui.ui.controls.LocalAppWindow
import pondui.WatchWindow
import pondui.WindowSize
import pondui.ui.core.ProvideAddressContext
import pondui.ui.nav.KeyCaster
import pondui.ui.nav.LocalKeyCaster

fun main() {
    _db = getRoomDatabase(getDatabaseBuilder())
    application {
        val cacheFlow = CacheFile("appcache.json") { AppCache() }
        val cache by cacheFlow.collectAsState()
        val keyCaster = remember { KeyCaster() }

        val windowState = WatchWindow(cache.windowSize) {
            cacheFlow.value = cacheFlow.value.copy(windowSize = it)
        }

        ProvideAddressContext(
            initialAddress = cache.address,
        ) {
            CompositionLocalProvider(LocalKeyCaster provides keyCaster) {
                Window(
                    state = windowState,
                    onCloseRequest = ::exitApplication,
                    title = "App",
                    undecorated = true,
                    onPreviewKeyEvent = keyCaster::keyEvent
                ) {
                    val baseDensity = LocalDensity.current
                    val density = Density(baseDensity.density * 1.0f, baseDensity.fontScale)
                    CompositionLocalProvider(
                        LocalDensity provides density
                    ) {
                        val appWindow = remember(cache.windowSize) {
                            AppWindow(cache.windowSize.width, cache.windowSize.height, density)
                        }
                        CompositionLocalProvider(LocalAppWindow provides appWindow) {
                            App(
                                changeRoute = { cacheFlow.value = cache.copy(address = it.toPath()) },
                                exitApp = ::exitApplication
                            )
                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class AppCache(
    val windowSize: WindowSize = WindowSize(600, 800),
    val address: String? = null,
    val lastSyncAt: Instant = Instant.DISTANT_FUTURE
)