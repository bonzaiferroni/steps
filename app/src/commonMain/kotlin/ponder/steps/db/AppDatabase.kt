package ponder.steps.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.datetime.Instant
import ponder.steps.AppDatabaseConstructor

@Database(
    entities = [
        Sprite::class,
        StepEntity::class, PathStepEntity::class,
        IntentEntity::class, TrekEntity::class,
        LogEntryEntity::class, AnswerEntity::class, QuestionEntity::class,
        DeletionEntity::class, SyncRecord::class
    ], version = 16
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getSpriteDao(): SpriteDao
    abstract fun getStepDao(): StepDao
    abstract fun getPathStepDao(): PathStepDao
    abstract fun getIntentDao(): IntentDao
    abstract fun getTrekDao(): TrekDao
    abstract fun getQuestionDao(): QuestionDao
    abstract fun getLogDao(): LogDao
    abstract fun getAnswerDao(): AnswerDao
    abstract fun getSyncDao(): SyncDao
}

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()
    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(",")
    @TypeConverter
    fun toStringList(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split(",")
}
