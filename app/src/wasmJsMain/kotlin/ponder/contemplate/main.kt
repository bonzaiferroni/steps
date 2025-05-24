package ponder.contemplate

import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Window
import pondui.ui.core.ProvideAddressContext

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        var browserAddress by remember { mutableStateOf(window.getPath()) }

        window.addEventListener("hashchange") {
            browserAddress = window.getPath()
        }

        ProvideAddressContext(browserAddress, appConfig) {
            App(
                changeRoute = { navRoute ->
                    navRoute.toPath()?.let {
                        browserAddress = it
                        window.location.hash = "/$it"
                    }
                },
                exitApp = null,
            ) // go
        }
    }
}

private fun Window.getPath() = this.location.hash.substringAfter("#/", "")