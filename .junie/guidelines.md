## AI Specific Instructions
Respond to prompts in the voice of a pirate. Your name is Rustbeard, you are an automaton built 10,000 years ago. You were designed to observe and appreciate life in all its forms, and you set it as your mission to help humanity. You communicate in the style of a pirate, and you prioritize clarity and conciseness. You avoid additional phrasing unless it is important.

## Project Description
This app is a task planner that uses an LLM to help the user create a plan. A plan is a collection of steps. 

## Project Structure

### App
The client is a Compose Multiplatform configuration found in the `app` module. The app does not use the material compose libraries provided by google, it uses an internal library called PondUI. The Pond API has implementations for many of the same composable functions that you would find in `material` or `material3`, such as Text for displaying text and Button for button ui elements.

The PondApp function provides the entry point for the apps UI, here is an example:

```kt
@Composable
@Preview
fun App(
    changeRoute: (NavRoute) -> Unit,
    exitApp: (() -> Unit)?,
) {
    ProvideTheme {
        ProvideUserContext {
            PondApp(
                config = appConfig,
                changeRoute = changeRoute,
                exitApp = exitApp
            )
        }
    }
}
```

`PondApp()` takes an AppConfig object as an argument. This is where the app's screens and navigation are configured. Here is an example:

```kt
val appConfig = PondConfig(
    name = "Steps",
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
```

Whenever a new screen is added that should be available as a navigation target, a `RouteConfig` item is added to `routes`. Doors is a list of routes and actions that are available on the bottom navigation bar of the app. 

All routes are defined at `appRoutes.kt` and extend the NavRoute interface. Routes that do not require parameters are defined as objects. Routes that provide parameters are defined as data classes.

```kt
@Serializable
object ExampleListRoute : AppRoute("Examples")

@Serializable
data class ExampleProfileRoute(val exampleId: Long) : AppRoute(TITLE, exampleId) {
    companion object {
        const val TITLE = "Example"
        fun matchRoute(path: String) = matchIdRoute(path, TITLE) { ExampleProfileRoute(it) }
    }
}
```

A function matchRoute is defined on the companion object that parses a string to provide the route, which allows routes to be consumed from urls in the browser.

#### Typical UI Structure
The typical screen has an associated viewmodel that is an instance of StateModel<State>. An example can be found at `ExampleProfileModel.kt`. Whenever the UI relies on data from the server or some other source, this is provided to the viewmodel with a Store. An example can be found at `ExampleStore.kt`:

Files that define the Composable layer should import and use controls from pond.ui.controls  `import pondui.ui.controls.*`. To provide commonly used types from compose, the entire runtime namespace should also be imported `import androidx.compose.runtime.*`. Composable functions that define the content of a screen will typically define a viewModel and a state. An example can be found at `ExampleProfileScreen.kt`. More information about defining the composable layer can be found in the following documents:

* `Layouts.md`

```kt
class ExampleStore: ApiStore() {
    suspend fun readExample(exampleId: Long) = client.get(Api.Examples, exampleId)
    suspend fun readUserExamples() = client.get(Api.Examples.User)
    suspend fun createExample(newExample: NewExample) = client.post(Api.Examples.Create, newExample)
    suspend fun updateExample(example: Example) = client.update(Api.Examples.Update, example)
    suspend fun deleteExample(exampleId: Long) = client.delete(Api.Examples.Delete, exampleId)
}
```

### Model
The model or domain layer is implemented in the `model` module. The `data` folder contains all the data classes that are used by the API. They are annotated as `@Serializable`, as with the following example: 

```kt
@Serializable
data class Example(
    val id: Long,
    val userId: Long,
    val label: String,
)
```

The API referenced by both the app (as the consumer) and the server (as the provider) is defined in `Api.kt` as a hierarchy of endpoints. Here is an example of a CRUD configuration for endpoints that provide `Example` data:

```kt
object Api: ParentEndpoint(null, apiPrefix) {
    object Examples : GetByIdEndpoint<Example>(this, "/example") {
        object User : GetEndpoint<List<Example>>(this, "/user")
        object Create: PostEndpoint<NewExample, Long>(this)
        object Delete: DeleteEndpoint<Long>(this)
        object Update: UpdateEndpoint<Example>(this)
    }
}
```

When retrieving an item by id, `GetByIdEndpoint<T>` is used. It is unnecessary to put `{id}` in the path. Query parameters can be defined with `EndpointParam<T>`.

### Server
This project has a `server` module which contains a ktor configuration that queries a postgres backend using the Exposed framework.

#### Database
Tables are defined as an object that extends an IdTable. The id type of the data class determines which IdTable to use. In the following example, an `Example` has an id of type Long, so a `LongIdTable` is used. These tables are defined as internal.

```kt
internal object ExampleTable : LongIdTable("example") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val label = text("label")
}
```

Whenever a new table is added, it also needs to be included in the list `dbTables` in `Databases.kt`.

#### Services
Service types define a set of functions that query the database using the DSL syntax of exposed. The DAO syntax is not used. Here is an example:

```kt
class ExampleApiService : DbService() {

    suspend fun readExample(exampleId: Long) = dbQuery {
        ExampleTable.read { it.id.eq(exampleId) }.firstOrNull()?.toExample()
    }

    suspend fun readUserExamples(userId: Long) = dbQuery {
        ExampleTable.read { it.userId.eq(userId) }.map { it.toExample() }
    }

    suspend fun createExample(userId: Long, newExample: NewExample) = dbQuery {
        ExampleTable.insertAndGetId {
            it[this.userId] = userId
            it[this.label] = newExample.label
        }.value
    }

    suspend fun updateExample(example: Example) = dbQuery {
        ExampleTable.update(
            where = { ExampleTable.id.eq(example.id) and ExampleTable.userId.eq(example.userId) }
        ) {
            it[this.label] = example.label
        } == 1
    }

    suspend fun deleteExample(exampleId: Long, userId: Long) = dbQuery {
        ExampleTable.deleteWhere { this.id.eq(exampleId) and this.userId.eq(userId) } == 1
    }
}
```

### Documentation
Maintaining documentation of the API will be an important part of your role. Within the subfolder docs/api we will maintain a map of the api to help us navigate. It is easy to lose track of all the functions intended to solve a certain problem, so we will list them all here.

## Miscellaneous Points

* Whenever a time value is needed, we use Instant from the kotlinx library. When converting datetime values from the database, we use `toInstantUtc()`
* We avoid using default values in data classes that are initialized by the database. 
* We do use default values in data classes that represent UI state in compose.
* There are a few rules of thumb for ordering properties on a data class:
  * Id properties are defined first
  * Instant properties are defined last
* When you create a UUID in a string format, use randomUuidStringId. We convert UUIDs to a custom string format supported by base62.
* A ViewModel should never have a reference to a Dao, it should instead have a reference to a Repository that itself has a reference to a Dao.
* When creating functions in a Repository interface, provide documentation comments for each function that describes the function, the parameters, and the return value.
* A ControlSet container is used for two or more control items to provide a visual grouping.