package ponder.steps.server.plugins

import io.ktor.server.application.*
import klutch.db.initDb
import klutch.db.tables.RefreshTokenTable
import klutch.db.tables.UserTable
import klutch.environment.readEnvFromPath
import ponder.steps.server.db.tables.DeletionTable
import ponder.steps.server.db.tables.ExampleTable
import ponder.steps.server.db.tables.IntentTable
import ponder.steps.server.db.tables.PathStepTable
import ponder.steps.server.db.tables.QuestionTable
import ponder.steps.server.db.tables.StepTable
import ponder.steps.server.db.tables.TrekTable

fun Application.configureDatabases() {
    initDb(env, dbTables) {
        exec(RecordUpdatedPgTrigger("step", "path_step", "question").buildSql())
        exec(RecordDeletionPgTrigger("step", "path_step", "question").buildSql())
        exec("""
            ALTER TABLE path_step DROP CONSTRAINT path_step_path_id_position_unique;
            ALTER TABLE path_step
                ADD CONSTRAINT path_step_path_id_position_unique
                UNIQUE(path_id, position)
                DEFERRABLE INITIALLY DEFERRED
        """.trimIndent())
    }
}

val env = readEnvFromPath()

val dbTables = listOf(
    UserTable,
    RefreshTokenTable,
    ExampleTable,
    StepTable,
    PathStepTable,
    IntentTable,
    TrekTable,
    DeletionTable,
    QuestionTable,
)

//CREATE DATABASE example_db;
//CREATE USER example_user WITH PASSWORD 'hunter2';
//ALTER DATABASE example_db OWNER TO example_user;
