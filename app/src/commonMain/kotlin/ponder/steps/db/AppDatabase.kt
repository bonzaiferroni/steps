package ponder.steps.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import ponder.steps.AppDatabaseConstructor

@Database(entities = [Sprite::class, StepEntity::class, PathStepEntity::class], version = 3)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getSpriteDao(): SpriteDao
    abstract fun getStepDao(): StepDao
}