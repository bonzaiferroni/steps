package ponder.steps

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

class RecordDeletionTrigger(
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
    CREATE TRIGGER IF NOT EXISTS record_deletion_$tableName
    AFTER DELETE ON $tableName
    FOR EACH ROW
    BEGIN
        INSERT INTO DeletionEntity (id, recordedAt)
        VALUES (
            OLD.id,
            CAST(strftime('%s','now') AS INTEGER)*1000
        );
    END;
""".trimIndent()