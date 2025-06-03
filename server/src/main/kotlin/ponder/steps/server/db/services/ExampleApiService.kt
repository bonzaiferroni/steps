package ponder.steps.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import ponder.steps.model.data.Example
import ponder.steps.model.data.NewExample
import ponder.steps.server.db.tables.ExampleTable
import ponder.steps.server.db.tables.toExample

class ExampleApiService : DbService() {

    suspend fun readExample(exampleId: Long) = dbQuery {
        ExampleTable.read { it.id.eq(exampleId) }.firstOrNull()?.toExample()
    }

    suspend fun readUserExamples(userId: String) = dbQuery {
        ExampleTable.read { it.userId.eq(userId) }.map { it.toExample() }
    }

    suspend fun createExample(userId: String, newExample: NewExample) = dbQuery {
        ExampleTable.insertAndGetId {
            it[this.userId] = userId.toUUID()
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

    suspend fun deleteExample(exampleId: Long, userId: String) = dbQuery {
        ExampleTable.deleteWhere { this.id.eq(exampleId) and this.userId.eq(userId) } == 1
    }
}