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

fun main() {
    application {
        val cacheFlow = CacheFile("appcache.json") { AppCache() }
        val cache by cacheFlow.collectAsState()

        val windowState = WatchWindow(cache.windowSize) {
            cacheFlow.value = cacheFlow.value.copy(windowSize = it)
        }

        ProvideAddressContext(
            initialAddress = cache.address,
        ) {
            Window(
                state = windowState,
                onCloseRequest = ::exitApplication,
                title = "App",
                undecorated = true,
            ) {
                App(
                    changeRoute = { cacheFlow.value = cache.copy(address = it.toPath()) },
                    exitApp = ::exitApplication
                )
            }
        }
    }
}

@Serializable
data class AppCache(
    val windowSize: WindowSize = WindowSize(600, 800),
    val address: String? = null
)