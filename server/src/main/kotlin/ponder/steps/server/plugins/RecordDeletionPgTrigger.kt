package ponder.steps.server.plugins

class RecordDeletionPgTrigger(vararg val tableNames: String) {
    fun buildSql() = buildString {
        appendLine(recordDeletionFunction)
        for (tableName in tableNames) {
            appendLine(toTrigger(tableName))
        }
    }
}

private val recordDeletionFunction = """
    CREATE OR REPLACE FUNCTION record_deletion()
        RETURNS TRIGGER AS $$
    BEGIN
        INSERT INTO deletion (id, user_id, recorded_at)
        VALUES (OLD.id, OLD.user_id, now()::timestamp);
        RETURN OLD;
    END;
    $$ LANGUAGE plpgsql;
""".trimIndent()

private fun toTrigger(tableName: String) = """
    DROP TRIGGER IF EXISTS trg_record_deletion_$tableName
        ON $tableName;
    
    CREATE TRIGGER trg_record_deletion_$tableName
        AFTER DELETE ON $tableName
        FOR EACH ROW
        EXECUTE FUNCTION record_deletion();
""".trimIndent()