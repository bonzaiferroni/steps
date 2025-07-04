package ponder.steps.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Instant
import ponder.steps.RecordDeletionTrigger
import ponder.steps.RecordUpdatedTrigger
import ponder.steps.db.TimeUnit
import ponder.steps.model.db.synchronizedData
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Database(
    entities = [
        Sprite::class,
        StepEntity::class, PathStepEntity::class,
        IntentEntity::class, TrekEntity::class,
        StepLogEntity::class, AnswerEntity::class, QuestionEntity::class,
        DeletionEntity::class, SyncRecord::class,
        TagEntity::class, StepTagEntity::class,
    ], version = 42
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
    abstract fun getLogDao(): StepLogDao
    abstract fun getAnswerDao(): AnswerDao
    abstract fun getSyncDao(): SyncDao
    abstract fun getTagDao(): TagDao
    abstract fun getStepTagDao(): StepTagDao
}

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()
    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)

    @TypeConverter
    fun fromDuration(value: Duration): Long = value.inWholeMilliseconds
    @TypeConverter
    fun toDuration(value: Long): Duration = value.milliseconds

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(",")
    @TypeConverter
    fun toStringList(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split(",")

    @TypeConverter
    fun fromTimeUnit(value: TimeUnit): String = value.toString()
    @TypeConverter
    fun toTimeUnit(value: String): TimeUnit = TimeUnit.valueOf(value)
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .addCallback(RecordUpdatedTrigger(synchronizedEntities))
        .addCallback(RecordDeletionTrigger(synchronizedEntities))
        // .addMigrations(MIGRATIONS)
        .fallbackToDestructiveMigration(true)
        // .fallbackToDestructiveMigrationOnDowngrade(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

private val synchronizedEntities = synchronizedData.map { "${it.simpleName!!}Entity" }