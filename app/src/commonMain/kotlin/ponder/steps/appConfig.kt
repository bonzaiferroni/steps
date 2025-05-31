package ponder.steps

import compose.icons.TablerIcons
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Home
import compose.icons.tablericons.List
import compose.icons.tablericons.Rocket
import compose.icons.tablericons.Star
import compose.icons.tablericons.YinYang
import kotlinx.collections.immutable.persistentListOf
import ponder.steps.ui.*
import pondui.ui.core.PondConfig
import pondui.ui.core.RouteConfig
import pondui.ui.nav.PortalDoor
import pondui.ui.nav.defaultScreen

val appConfig = PondConfig(
    name = "Steps",
    logo = TablerIcons.Heart,
    home = StartRoute,
    routes = persistentListOf(
        RouteConfig(StartRoute::matchRoute) { defaultScreen<StartRoute> { StartScreen() } },
        RouteConfig(HelloRoute::matchRoute) { defaultScreen<HelloRoute> { HelloScreen() } },
        RouteConfig(ExampleListRoute::matchRoute) { defaultScreen<ExampleListRoute> { ExampleListScreen() } },
        RouteConfig(ExampleProfileRoute::matchRoute) { defaultScreen<ExampleProfileRoute> { ExampleProfileScreen(it) } },
        RouteConfig(PathRoute::matchRoute) { defaultScreen<PathRoute> { PathScreen() } },
        RouteConfig(GeminiRoute::matchRoute) { defaultScreen<GeminiRoute> { GeminiScreen() } },
        RouteConfig(IntentionRoute::matchRoute) { defaultScreen<IntentionRoute> { IntentionScreen() }},
        RouteConfig(JourneyRoute::matchRoute) { defaultScreen<JourneyRoute> { JourneyScreen() }}
    ),
    doors = persistentListOf(
        PortalDoor(TablerIcons.Home, StartRoute),
        PortalDoor(TablerIcons.YinYang, HelloRoute),
        PortalDoor(TablerIcons.Rocket, ExampleListRoute),
        PortalDoor(TablerIcons.List, PathRoute(), "Roots"),
        PortalDoor(TablerIcons.Star, GeminiRoute),
    ),
)
