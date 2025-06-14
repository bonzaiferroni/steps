package ponder.steps

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

class RecordUpdatedTrigger(
    vararg val tableNames: String
) : RoomDatabase.Callback() {
    override fun onOpen(connection: SQLiteConnection) {
        super.onOpen(connection)
        for (tableName in tableNames) {
            connection.execSQL(toTrigger(tableName))
        }
    }
}

private fun toTrigger(tableName: String) = """
    CREATE TRIGGER IF NOT EXISTS record_updated_$tableName
    AFTER UPDATE ON $tableName
    WHEN NEW.updatedAt = OLD.updatedAt
    BEGIN
        UPDATE $tableName
        SET updatedAt = CAST(strftime('%s','now') AS INTEGER) * 1000
        WHERE rowid = NEW.rowid;
    END;
""".trimIndent()