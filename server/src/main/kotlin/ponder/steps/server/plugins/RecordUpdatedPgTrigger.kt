package ponder.steps.server.plugins

class RecordUpdatedPgTrigger(val tableNames: List<String>) {
    fun buildSql() = buildString {
        appendLine(recordUpdatedAtFunction)
        for (tableName in tableNames) {
            appendLine(toTrigger(tableName))
        }
    }
}

private val recordUpdatedAtFunction = """
    CREATE OR REPLACE FUNCTION record_updated_at_if_unchanged() 
        RETURNS TRIGGER AS $$
    BEGIN
        IF NEW.updated_at = OLD.updated_at THEN
            NEW.updated_at := now()::timestamp;
        END IF;
        RETURN NEW;
    END;
    $$ LANGUAGE plpgsql;
""".trimIndent()

private fun toTrigger(tableName: String) = """
    DROP TRIGGER IF EXISTS trg_record_updated_at_$tableName
        ON $tableName;
  
    CREATE TRIGGER trg_record_updated_at_$tableName
        BEFORE UPDATE ON $tableName
        FOR EACH ROW
        EXECUTE FUNCTION record_updated_at_if_unchanged();
""".trimIndent()