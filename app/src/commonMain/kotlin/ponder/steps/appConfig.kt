package ponder.steps

import compose.icons.TablerIcons
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Home
import compose.icons.tablericons.List
import compose.icons.tablericons.Planet
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Walk
import compose.icons.tablericons.Wind
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
        RouteConfig(PathsRoute::matchRoute) { defaultScreen<PathsRoute> { PathsScreen(it) } },
        RouteConfig(GeminiRoute::matchRoute) { defaultScreen<GeminiRoute> { GeminiScreen() } },
        RouteConfig(JourneyRoute::matchRoute) { defaultScreen<JourneyRoute> { JourneyScreen() }},
        RouteConfig(SpriteRoute::matchRoute) { defaultScreen<SpriteRoute> { SpriteScreen() }},
        RouteConfig(SettingsRoute::matchRoute) { defaultScreen<SettingsRoute> { SettingsScreen() } }
    ),
    doors = persistentListOf(
        // PortalDoor(TablerIcons.Home, StartRoute),
        // PortalDoor(TablerIcons.YinYang, HelloRoute),
        PortalDoor(TablerIcons.Walk, JourneyRoute),
        PortalDoor(TablerIcons.List, PathsRoute()),
        PortalDoor(TablerIcons.Planet, GeminiRoute),
        PortalDoor(TablerIcons.Settings, SettingsRoute),
        // PortalDoor(TablerIcons.Wind, SpriteRoute)
    ),
)
