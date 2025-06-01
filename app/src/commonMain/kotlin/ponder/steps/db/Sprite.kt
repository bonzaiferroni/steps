package ponder.steps.db

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity
data class Sprite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "speed")
    val speed: Int
)

@Dao
interface SpriteDao {
    @Insert
    suspend fun insert(sprite: Sprite)
    @Query("SELECT count(*) FROM Sprite")
    suspend fun count(): Int
    @Query("SELECT * FROM Sprite")
    fun getAllAsFlow(): Flow<List<Sprite>>
}