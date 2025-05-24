package ponder.steps

import kotlinx.serialization.Serializable
import pondui.ui.nav.AppRoute
import pondui.ui.nav.matchIdRoute

@Serializable
object StartRoute : AppRoute("Start")

@Serializable
object HelloRoute : AppRoute("Hello")

@Serializable
object ExampleListRoute : AppRoute("Examples")

@Serializable
object RootStepsRoute : AppRoute("Root Steps")

@Serializable
data class ExampleProfileRoute(val exampleId: Long) : AppRoute(TITLE, exampleId) {
    companion object {
        const val TITLE = "Example"
        fun matchRoute(path: String) = matchIdRoute(path, TITLE) { ExampleProfileRoute(it) }
    }
}
