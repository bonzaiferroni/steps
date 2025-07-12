package ponder.steps.server.plugins

import ponder.steps.model.data.SyncType

class RecordDeletionPgTrigger() {
    fun buildSql() = buildString {
        appendLine(recordDeletionFunction)
        for (syncType in SyncType.entries) {
            appendLine(toTrigger(syncType))
        }
    }
}

private val recordDeletionFunction = """
    CREATE OR REPLACE FUNCTION record_deletion()
        RETURNS TRIGGER AS $$
    DECLARE
        entity_name text := TG_ARGV[0];
    BEGIN
        INSERT INTO deletion (id, user_id, entity_name, deleted_at)
        VALUES (OLD.id, OLD.user_id, entity_name, now()::timestamp)
        ON CONFLICT (id) DO UPDATE
            SET deleted_at = EXCLUDED.deleted_at;
        RETURN OLD;
    END;
    $$ LANGUAGE plpgsql;
""".trimIndent()

private fun toTrigger(syncType: SyncType) = """
    DROP TRIGGER IF EXISTS trg_record_deletion_${syncType.snakeName}
        ON ${syncType.snakeName};
    
    CREATE TRIGGER trg_record_deletion_${syncType.snakeName}
        AFTER DELETE ON ${syncType.snakeName}
        FOR EACH ROW
        EXECUTE FUNCTION record_deletion('${syncType.className}');
""".trimIndent()