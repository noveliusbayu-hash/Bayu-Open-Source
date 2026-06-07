package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "sparks")
data class Spark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
    val vibe: String = "Inspiration",
    val isFavorite: Boolean = false
)

@Dao
interface SparkDao {
    @Query("SELECT * FROM sparks ORDER BY timestamp DESC")
    fun getAllSparks(): Flow<List<Spark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpark(spark: Spark)

    @Query("DELETE FROM sparks WHERE id = :id")
    suspend fun deleteSparkById(id: Int)

    @Query("UPDATE sparks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)
}

@Database(entities = [Spark::class], version = 1, exportSchema = false)
abstract class SparkDatabase : RoomDatabase() {
    abstract fun sparkDao(): SparkDao
}
