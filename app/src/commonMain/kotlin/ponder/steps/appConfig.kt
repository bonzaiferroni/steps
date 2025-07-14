package ponder.steps

import compose.icons.TablerIcons
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.Heart
import compose.icons.tablericons.List
import compose.icons.tablericons.Message
import compose.icons.tablericons.Pencil
import compose.icons.tablericons.Settings
import compose.icons.tablericons.Walk
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
        RouteConfig(PathsRoute::matchRoute) { defaultScreen<PathsRoute> { PathsScreen() }},
        RouteConfig(StepProfileRoute::matchRoute) { defaultScreen<StepProfileRoute> { StepProfileScreen(it) } },
        RouteConfig(GeminiRoute::matchRoute) { defaultScreen<GeminiRoute> { GeminiScreen() } },
        RouteConfig(TodoRoute::matchRoute) { defaultScreen<TodoRoute> { TodoScreen() }},
        RouteConfig(PlanRoute::matchRoute) { defaultScreen<PlanRoute> { PlanScreen() }},
        RouteConfig(SpriteRoute::matchRoute) { defaultScreen<SpriteRoute> { SpriteScreen() }},
        RouteConfig(SettingsRoute::matchRoute) { defaultScreen<SettingsRoute> { SettingsScreen() } },
        RouteConfig(ChatRoute::matchRoute) { defaultScreen<ChatRoute> { ChatScreen() }},
        RouteConfig(LogRoute::matchRoute) { defaultScreen<LogRoute> { LineLogView() }}
    ),
    doors = persistentListOf(
        // PortalDoor(TablerIcons.Home, StartRoute),
        // PortalDoor(TablerIcons.YinYang, HelloRoute),
        PortalDoor(TablerIcons.Walk, TodoRoute),
        PortalDoor(TablerIcons.List, PathsRoute),
        // PortalDoor(TablerIcons.Planet, GeminiRoute),
        PortalDoor(TablerIcons.Calendar, PlanRoute),
        PortalDoor(TablerIcons.Pencil, LogRoute),
        // PortalDoor(TablerIcons.Message, ChatRoute),
        PortalDoor(TablerIcons.Settings, SettingsRoute),
        // PortalDoor(TablerIcons.Wind, SpriteRoute)
    ),
)
