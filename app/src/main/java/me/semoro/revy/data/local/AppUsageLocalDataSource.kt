package me.semoro.revy.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.semoro.revy.data.local.room.AppUsageDao
import me.semoro.revy.data.local.room.AppUsageEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for accessing local app usage data.
 */
interface AppUsageLocalDataSource {
    /**
     * Get all app usage records.
     *
     * @return Flow of list of all app usage records
     */
    fun getAllAppUsage(): Flow<List<AppUsageEntity>>

    /**
     * Get app usage record for a specific package.
     *
     * @param packageName The package name to query
     * @return The app usage entity for the specified package, or null if not found
     */
    suspend fun getAppUsage(packageName: String): AppUsageEntity?

    /**
     * Update the last used timestamp for an app.
     *
     * @param packageName The package name of the app
     * @param timestamp The new timestamp
     */
    suspend fun updateLastUsedTimestamp(packageName: String, timestamp: Long)

    /**
     * Insert or update an app usage record.
     *
     * @param packageName The package name of the app
     * @param timestamp The timestamp when the app was last used
     */
    suspend fun insertOrUpdateAppUsage(packageName: String, timestamp: Long)
}

/**
 * Implementation of AppUsageLocalDataSource that uses Room.
 */
@Singleton
class AppUsageLocalDataSourceImpl @Inject constructor(
    private val appUsageDao: AppUsageDao
) : AppUsageLocalDataSource {

    override fun getAllAppUsage(): Flow<List<AppUsageEntity>> {
        return appUsageDao.getAllAppUsage()
    }

    override suspend fun getAppUsage(packageName: String): AppUsageEntity? {
        return appUsageDao.getAppUsage(packageName)
    }

    override suspend fun updateLastUsedTimestamp(packageName: String, timestamp: Long) {
        val updated = appUsageDao.updateLastUsedTimestamp(packageName, timestamp)
        if (updated == 0) {
            // If no rows were updated, the app doesn't exist in the database yet
            insertOrUpdateAppUsage(packageName, timestamp)
        }
    }

    override suspend fun insertOrUpdateAppUsage(packageName: String, timestamp: Long) {
        val appUsage = AppUsageEntity(
            packageName = packageName,
            lastUsedTimestamp = timestamp
        )
        appUsageDao.insertOrUpdate(appUsage)
    }
}