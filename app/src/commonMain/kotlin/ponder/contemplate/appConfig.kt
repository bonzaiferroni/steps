package ponder.contemplate

import compose.icons.TablerIcons
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Home
import compose.icons.tablericons.Rocket
import compose.icons.tablericons.YinYang
import kotlinx.collections.immutable.persistentListOf
import ponder.contemplate.ui.ExampleListScreen
import ponder.contemplate.ui.ExampleProfileScreen
import ponder.contemplate.ui.HelloScreen
import ponder.contemplate.ui.StartScreen
import pondui.ui.core.PondConfig
import pondui.ui.core.RouteConfig
import pondui.ui.nav.PortalDoor
import pondui.ui.nav.defaultScreen

val appConfig = PondConfig(
    name = "Contemplate",
    logo = TablerIcons.Heart,
    home = StartRoute,
    routes = persistentListOf(
        RouteConfig(StartRoute::matchRoute) { defaultScreen<StartRoute> { StartScreen() } },
        RouteConfig(HelloRoute::matchRoute) { defaultScreen<HelloRoute> { HelloScreen() } },
        RouteConfig(ExampleListRoute::matchRoute) { defaultScreen<ExampleListRoute> { ExampleListScreen() } },
        RouteConfig(ExampleProfileRoute::matchRoute) { defaultScreen<ExampleProfileRoute> { ExampleProfileScreen(it) } }
    ),
    doors = persistentListOf(
        PortalDoor(TablerIcons.Home, StartRoute),
        PortalDoor(TablerIcons.YinYang, HelloRoute),
        PortalDoor(TablerIcons.Rocket, ExampleListRoute),
    ),
)