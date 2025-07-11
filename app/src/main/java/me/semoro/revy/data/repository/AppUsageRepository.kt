package me.semoro.revy.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.semoro.revy.data.local.AppUsageDataSource
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.model.RecencyBucket
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for accessing app usage data.
 */
interface AppUsageRepository {
    /**
     * Gets a flow of all installed apps sorted by recency.
     *
     * @return Flow of list of AppInfo objects sorted by recency
     */
    fun getInstalledAppsByRecency(): Flow<List<AppInfo>>
    
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
}

/**
 * Implementation of AppUsageRepository.
 */
@Singleton
class AppUsageRepositoryImpl @Inject constructor(
    private val appUsageDataSource: AppUsageDataSource
) : AppUsageRepository {
    
    override fun getInstalledAppsByRecency(): Flow<List<AppInfo>> = flow {
        val apps = appUsageDataSource.getInstalledApps()
        val sortedApps = apps.sortedByDescending { it.lastUsedTimestamp }
        emit(sortedApps)
    }
    
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
}