package me.semoro.revy.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the app_usage table.
 */
@Dao
interface AppUsageDao {
    /**
     * Insert or update an app usage record.
     *
     * @param appUsage The app usage entity to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(appUsage: AppUsageEntity)

    /**
     * Get all app usage records.
     *
     * @return Flow of list of all app usage entities
     */
    @Query("SELECT * FROM app_usage ORDER BY lastUsedTimestamp DESC")
    fun getAllAppUsage(): Flow<List<AppUsageEntity>>

    /**
     * Get app usage record for a specific package.
     *
     * @param packageName The package name to query
     * @return The app usage entity for the specified package, or null if not found
     */
    @Query("SELECT * FROM app_usage WHERE packageName = :packageName")
    suspend fun getAppUsage(packageName: String): AppUsageEntity?

    /**
     * Update the last used timestamp for an app.
     *
     * @param packageName The package name of the app
     * @param timestamp The new timestamp
     * @return The number of rows updated
     */
    @Query("UPDATE app_usage SET lastUsedTimestamp = :timestamp WHERE packageName = :packageName AND lastUsedTimestamp < :timestamp")
    suspend fun updateLastUsedTimestamp(packageName: String, timestamp: Long): Int

    @Query("DELETE FROM app_usage WHERE packageName = :packageName")
    suspend fun removeByPackageName(packageName: String)
}