# Steps Project Development Guidelines

## Build/Configuration Instructions

### Prerequisites
- JDK 21 or higher
- PostgreSQL database
- Google Gemini API key

### Database Setup
1. Create a PostgreSQL database:
```sql
CREATE DATABASE steps_db;
CREATE USER steps_user WITH PASSWORD 'your_password';
ALTER DATABASE steps_db OWNER TO steps_user;
```

2. Create a `.env` file in the project root with the following variables:
```
DB_URL=jdbc:postgresql://localhost:5432/steps_db
DB_USER=steps_user
DB_PW=your_password
APP_SECRET=your_secret_key
GEMINI_KEY_RATE_LIMIT_A=your_gemini_api_key_1
GEMINI_KEY_RATE_LIMIT_B=your_gemini_api_key_2
```

### Building and Running
- **Desktop Application**: `./gradlew :app:run`
- **Android Application**: `./gradlew :app:installDebug`
- **Server**: `./gradlew :server:run`

## Testing Information

The project has limited test coverage, with tests primarily in the library modules:
- `kabinet/library/src/commonTest` - Tests for the Kabinet library
- `klutch/library` - Uses kotlin.test, kotlinx-coroutines-test, and ktor-client-mock
- `server` - Uses kotlin.test.junit

When implementing new features, consider adding tests using these frameworks. The project would benefit from more comprehensive test coverage, especially for core functionality.

## Development Information

### Project Structure
- **app**: Compose Multiplatform client application (Android, Desktop)
- **model**: Shared data models and API definitions
- **server**: Ktor server with PostgreSQL database
- **pondui**: UI component library
- **kabinet**: Utility library
- **klutch**: Utility library

### Code Style and Patterns

#### Data Models
- Data classes are annotated with `@Serializable` for kotlinx.serialization
- ID fields are defined first, followed by content fields, with time fields at the end
- Nullable types are used for optional fields
- Time values use `kotlinx.datetime.Instant`
- Default values are avoided in data classes initialized by the database
- Default values are used in data classes representing UI state

#### API Definition
- APIs are defined in a hierarchical structure in `model/src/commonMain/kotlin/ponder/steps/model/Api.kt`
- Endpoints are typed with input and output types (e.g., `PostEndpoint<Input, Output>`)
- The endpoint hierarchy matches the URL structure

#### Synchronization System
- Uses a polymorphic serialization system with CBOR format
- `SyncRecord` interface is implemented by various data models
- `SyncFrame` interface is implemented by `SyncPacket` and `SyncHandshake`

#### Database
- Tables are defined as objects extending `IdTable` classes
- New tables must be added to the `dbTables` list in `server/src/main/kotlin/ponder/steps/server/plugins/Databases.kt`
- Uses Exposed framework for database access with DSL syntax (not DAO)

#### UI
- Uses PondUI library instead of Material Compose
- Screens typically have an associated ViewModel extending `StateModel<State>`
- Data is provided to ViewModels through Stores

### Important Conventions
- UUIDs in string format should use `randomUuidStringId` (base62 format)
- ViewModels should never reference DAOs directly, use Repositories instead
- When creating functions in Repository interfaces, provide documentation comments
- When creating functions in LocalRepository that call DAO functions, assign directly with `=` operator