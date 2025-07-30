package me.semoro.revy.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.graphics.drawable.toBitmap
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import me.semoro.revy.data.local.room.AppUsageDao
import me.semoro.revy.data.local.room.AppUsageEntity
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.model.RecencyBucket
import me.semoro.revy.data.model.SlotInfo
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit

/**
 * Repository interface for accessing app usage data.
 */
interface AppUsageRepository {
    /**
     * Gets a flow of apps grouped by recency buckets.
     *
     * @return Flow of map of RecencyBucket to list of AppInfo objects
     */
    fun getAppsWithUsageInfo(): Flow<List<AppInfo>>

    /**
     * Records that an app was launched.
     *
     * @param packageName The package name of the app
     */
    suspend fun recordAppLaunch(packageName: String)


    suspend fun removeUsageRecord(packageName: String)
    /**
     * Checks for app usage activity on the opening of the main app screen.
     * This should be called when the main screen is opened.
     */
    suspend fun checkAppUsageActivity()
}



data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val icon: Bitmap,
)

/**
 * Implementation of AppUsageRepository.
 */
@Singleton
class AppUsageRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appUsageDao: AppUsageDao
) : AppUsageRepository {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private var lastUsageCheckStamp = System.currentTimeMillis() - 1.days.toLong(DurationUnit.MILLISECONDS)

    private val appsWithUsageInfo = moleculeFlow(RecompositionMode.Immediate) {
        val packageManager = context.packageManager
        val activities: List<InstalledAppInfo> =
            remember {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                val flags = PackageManager.ResolveInfoFlags.of(
                    PackageManager.MATCH_ALL.toLong())

                // Create AppInfo objects for each installed app
                context.packageManager.queryIntentActivities(intent, flags).mapNotNull { appInfo ->
                    try {
                        val packageName = appInfo.activityInfo.packageName

                        InstalledAppInfo(
                            packageName = packageName,
                            label = appInfo.loadLabel(packageManager).toString(),
                            icon = appInfo.loadIcon(packageManager).toBitmap(),
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
            }

        val appUsages by appUsageDao.getAllAppUsage().collectAsState(emptyList())
        val associated = appUsages.associateBy { it.packageName }
        val appInfos = activities.map {
            AppInfo(
                it.packageName,
                it.label,
                it.icon,
                associated[it.packageName]?.lastUsedTimestamp ?: 0L
            )
        }

        // Sort apps within each bucket by recency
        val sortedApps = appInfos.sortedByDescending { it.lastUsedTimestamp }
        sortedApps
    }


    override fun getAppsWithUsageInfo(): Flow<List<AppInfo>> = appsWithUsageInfo


    override suspend fun removeUsageRecord(packageName: String) {
        appUsageDao.removeByPackageName(packageName)
    }

    private suspend fun updateOrRecordLastUsedTime(packageName: String, timeStamp: Long) {
        val update = appUsageDao.updateLastUsedTimestamp(packageName, timeStamp)
        if (update == 0) {
            appUsageDao.insertOrUpdate(AppUsageEntity(packageName, timeStamp))
        }
    }

    override suspend fun recordAppLaunch(packageName: String) {
        val timestamp = System.currentTimeMillis()
        updateOrRecordLastUsedTime(packageName, timestamp)
    }

    override suspend fun checkAppUsageActivity() {
        // Get usage stats for the last 30 days
        val endTime = System.currentTimeMillis()
        val startTime = lastUsageCheckStamp // 1 day in milliseconds
        val usageEvents = usageStatsManager.queryEvents(
            startTime, endTime
        )

        if (usageEvents == null) {
            println("Error, usage events is inaccessible")
            return
        }

        // swap last usage check time
        lastUsageCheckStamp = endTime

        val compacted = mutableMapOf<String, Long>()
        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                compacted[event.packageName] =
                    compacted[event.packageName]?.let { other ->
                        max(other, event.timeStamp)
                    } ?: event.timeStamp
            }
        }

        for ((packageName, timeStamp) in compacted) {
            updateOrRecordLastUsedTime(packageName, timeStamp)
        }
    }
}
