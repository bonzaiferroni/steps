package ponder.steps

import kotlinx.serialization.Serializable
import pondui.ui.nav.AppRoute
import pondui.ui.nav.IdRoute
import pondui.ui.nav.matchLongIdRoute
import pondui.ui.nav.matchStringIdRoute

@Serializable
object StartRoute : AppRoute("Start")

@Serializable
object HelloRoute : AppRoute("Hello")

@Serializable
object ExampleListRoute : AppRoute("Examples")

@Serializable
data class PathRoute(val pathId: String? = null) : IdRoute<String>(TITLE, pathId) {
    companion object {
        const val TITLE = "Paths"
        fun matchRoute(path: String) = matchStringIdRoute(path, TITLE) { PathRoute(it) }
    }
}

@Serializable
object GeminiRoute : AppRoute("Gemini")

@Serializable
object IntentionRoute : AppRoute("Intention")

@Serializable
object JourneyRoute : AppRoute("Journey")

@Serializable
object FocusRoute : AppRoute("Focus")

@Serializable
data class ExampleProfileRoute(val exampleId: Long) : IdRoute<Long>(TITLE, exampleId) {
    companion object {
        const val TITLE = "Example"
        fun matchRoute(path: String) = matchLongIdRoute(path, TITLE) { ExampleProfileRoute(it) }
    }
}
