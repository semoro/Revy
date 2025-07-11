package me.semoro.revy.data.local

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import dagger.hilt.android.qualifiers.ApplicationContext
import me.semoro.revy.data.model.AppInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for accessing app usage statistics.
 */
interface AppUsageDataSource {
    /**
     * Gets a list of all installed apps with their usage statistics.
     *
     * @return List of AppInfo objects containing app details and usage statistics
     */
    suspend fun getInstalledApps(): List<AppInfo>
    
    /**
     * Checks if the app has permission to access usage statistics.
     *
     * @return true if the app has permission, false otherwise
     */
    fun hasUsageStatsPermission(): Boolean
}

/**
 * Implementation of AppUsageDataSource that uses UsageStatsManager.
 */
@Singleton
class AppUsageDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppUsageDataSource {
    
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager
    
    override suspend fun getInstalledApps(): List<AppInfo> {

        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val flags = PackageManager.ResolveInfoFlags.of(
            PackageManager.MATCH_ALL.toLong())
        val activities: List<ResolveInfo> =
            context.packageManager.queryIntentActivities(intent, flags)



        // Get usage stats for the last 30 days
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (30 * 24 * 60 * 60 * 1000L) // 30 days in milliseconds
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        
        // Create a map of package name to last used time
        val usageMap = usageStats.associateBy(
            { it.packageName },
            { it.lastTimeUsed }
        )

        println(usageMap)
        // Create AppInfo objects for each installed app
        return activities.mapNotNull { appInfo ->
            try {
                val packageName = appInfo.activityInfo.packageName
                val lastUsedTime = usageMap[packageName] ?: 0L

                AppInfo(
                    packageName = packageName,
                    label = appInfo.loadLabel(packageManager).toString(),
                    icon = appInfo.loadIcon(packageManager),
                    lastUsedTimestamp = lastUsedTime
                )
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override fun hasUsageStatsPermission(): Boolean {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000 * 60 * 60) // 1 hour in milliseconds
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        return usageStats.isNotEmpty()
    }

}