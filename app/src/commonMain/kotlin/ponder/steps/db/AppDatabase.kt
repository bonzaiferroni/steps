package ponder.steps.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import ponder.steps.AppDatabaseConstructor
import ponder.steps.model.data.Step
import ponder.steps.model.data.StepEntity

@Database(entities = [Sprite::class, Step::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getSpriteDao(): SpriteDao
}