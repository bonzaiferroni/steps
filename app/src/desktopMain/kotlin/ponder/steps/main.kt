package ponder.steps

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.serialization.Serializable
import pondui.CacheFile
import pondui.WatchWindow
import pondui.WindowSize
import pondui.ui.core.ProvideAddressContext
import pondui.ui.nav.AppRoute
import pondui.ui.nav.KeyCaster
import pondui.ui.nav.LocalKeyCaster

fun main() {
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
                    App(
                        changeRoute = { cacheFlow.value = cache.copy(address = it.toPath()) },
                        exitApp = ::exitApplication
                    )
                }
            }
        }
    }
}

@Serializable
data class AppCache(
    val windowSize: WindowSize = WindowSize(600, 800),
    val address: String? = null
)