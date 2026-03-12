package me.semoro.revy.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppFrequencyScoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: AppFrequencyScoreEntity)

    @Query("SELECT * FROM app_frequency_score ORDER BY score DESC")
    fun getAll(): Flow<List<AppFrequencyScoreEntity>>

    @Query("SELECT * FROM app_frequency_score WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): AppFrequencyScoreEntity?

    @Query("SELECT * FROM app_frequency_score WHERE nextUpdateTime <= :currentTime ORDER BY nextUpdateTime ASC")
    suspend fun getStaleEntries(currentTime: Long): List<AppFrequencyScoreEntity>

    @Query("DELETE FROM app_frequency_score WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}
