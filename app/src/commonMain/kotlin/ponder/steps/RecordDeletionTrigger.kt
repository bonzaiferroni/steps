package ponder.steps

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import ponder.steps.model.data.SyncType

class RecordDeletionTrigger() : RoomDatabase.Callback() {
    override fun onOpen(connection: SQLiteConnection) {
        super.onOpen(connection)
        for (syncType in SyncType.entries) {
            connection.execSQL(toTrigger(syncType))
        }
    }
}

private fun toTrigger(syncType: SyncType) = """
    CREATE TRIGGER IF NOT EXISTS record_deletion_${syncType.entityName}
    AFTER DELETE ON ${syncType.entityName}
    FOR EACH ROW
    BEGIN
        INSERT INTO DeletionEntity (id, entity, deletedAt)
        VALUES (
            OLD.id,
            '${syncType.className}',
            CAST((julianday('now') - 2440587.5) * 86400000 AS INTEGER)
        )
        ON CONFLICT(id) DO UPDATE
            SET
                entity     = excluded.entity,
                deletedAt  = excluded.deletedAt;
    END;
""".trimIndent()