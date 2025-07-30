package me.semoro.revy.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.semoro.revy.data.local.AppUsageDataSource
import me.semoro.revy.data.local.AppUsageLocalDataSource
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.model.RecencyBucket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for accessing app usage data.
 */
interface AppUsageRepository {
    /**
     * Gets a flow of apps grouped by recency buckets.
     *
     * @return Flow of map of RecencyBucket to list of AppInfo objects
     */
    fun getAppsByRecencyBucket(): Flow<Map<RecencyBucket, List<AppInfo>>>

    /**
     * Checks if the app has permission to access usage statistics.
     *
     * @return true if the app has permission, false otherwise
     */
    fun hasUsageStatsPermission(): Boolean

    /**
     * Records that an app was launched.
     *
     * @param packageName The package name of the app
     */
    suspend fun recordAppLaunch(packageName: String)

    /**
     * Checks for app usage activity on the opening of the main app screen.
     * This should be called when the main screen is opened.
     */
    suspend fun checkAppUsageActivity()
}

/**
 * Implementation of AppUsageRepository.
 */
@Singleton
class AppUsageRepositoryImpl @Inject constructor(
    private val appUsageDataSource: AppUsageDataSource,
    private val appUsageLocalDataSource: AppUsageLocalDataSource
) : AppUsageRepository {

    override fun getAppsByRecencyBucket(): Flow<Map<RecencyBucket, List<AppInfo>>> = flow {
        val apps = appUsageDataSource.getInstalledApps()
        val now = System.currentTimeMillis()

        // Group apps by recency bucket
        val groupedApps = apps.groupBy { app ->
            RecencyBucket.fromTimestamp(app.lastUsedTimestamp)
        }

        // Sort apps within each bucket by recency
        val sortedGroupedApps = groupedApps.mapValues { (_, apps) ->
            apps.sortedByDescending { it.lastUsedTimestamp }
        }

        emit(sortedGroupedApps)
    }

    override fun hasUsageStatsPermission(): Boolean {
        return appUsageDataSource.hasUsageStatsPermission()
    }

    override suspend fun recordAppLaunch(packageName: String) {
        val timestamp = System.currentTimeMillis()
        appUsageLocalDataSource.updateLastUsedTimestamp(packageName, timestamp)
    }

    override suspend fun checkAppUsageActivity() {
        // This method is called when the main screen is opened
        // Get the latest app usage data from the system
        val installedApps = appUsageDataSource.getInstalledApps()

        // Update the local database with the latest usage data
        for (app in installedApps) {
            // Only update if the app has been used (timestamp > 0)
            if (app.lastUsedTimestamp > 0) {
                // Check if we already have a record for this app
                val existingAppUsage = appUsageLocalDataSource.getAppUsage(app.packageName)

                // Only update if the system timestamp is more recent than our stored timestamp
                if (existingAppUsage == null || app.lastUsedTimestamp > existingAppUsage.lastUsedTimestamp) {
                    appUsageLocalDataSource.updateLastUsedTimestamp(app.packageName, app.lastUsedTimestamp)
                }
            }
        }
    }
}
