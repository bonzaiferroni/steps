package ponder.contemplate.server.db.services

import klutch.db.DbService
import klutch.db.read
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import ponder.contemplate.model.data.Example
import ponder.contemplate.model.data.NewExample
import ponder.contemplate.server.db.tables.ExampleTable
import ponder.contemplate.server.db.tables.toExample

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
            it[this.symtrix] = newExample.symtrix
        }.value
    }

    suspend fun updateExample(example: Example) = dbQuery {
        ExampleTable.update(
            where = { ExampleTable.id.eq(example.id) and ExampleTable.userId.eq(example.userId) }
        ) {
            it[this.symtrix] = example.symtrix
        } == 1
    }

    suspend fun deleteExample(exampleId: Long, userId: Long) = dbQuery {
        ExampleTable.deleteWhere { this.id.eq(exampleId) and this.userId.eq(userId) } == 1
    }
}