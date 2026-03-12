package me.semoro.revy.data.repository

import kotlinx.coroutines.flow.Flow
import me.semoro.revy.data.local.room.AppFrequencyScoreDao
import me.semoro.revy.data.local.room.AppFrequencyScoreEntity
import me.semoro.revy.data.local.room.AppUsageEventDao
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

data class FrequencyScoreBreakdown(
    val count3d: Int,
    val count10d: Int,
    val count30d: Int,
    val score: Double
)

interface AppFrequencyRepository {
    fun getAppsByFrequency(): Flow<List<AppFrequencyScoreEntity>>
    suspend fun recalculateScore(packageName: String)
    suspend fun processStaleScores()
    suspend fun initializeAllScores(packageNames: List<String>)
    suspend fun getScoreBreakdown(packageName: String): FrequencyScoreBreakdown
}

@Singleton
class AppFrequencyRepositoryImpl @Inject constructor(
    private val appFrequencyScoreDao: AppFrequencyScoreDao,
    private val appUsageEventDao: AppUsageEventDao
) : AppFrequencyRepository {

    override fun getAppsByFrequency(): Flow<List<AppFrequencyScoreEntity>> {
        return appFrequencyScoreDao.getAll()
    }

    override suspend fun recalculateScore(packageName: String) {
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - TimeUnit.DAYS.toMillis(3)
        val tenDaysAgo = now - TimeUnit.DAYS.toMillis(10)
        val thirtyDaysAgo = now - TimeUnit.DAYS.toMillis(30)

        val count3d = appUsageEventDao.countEventsSince(packageName, threeDaysAgo)
        val count10d = appUsageEventDao.countEventsSince(packageName, tenDaysAgo)
        val count30d = appUsageEventDao.countEventsSince(packageName, thirtyDaysAgo)

        val score = (count3d / 3.0) * 0.5 + (count10d / 10.0) * 0.3 + (count30d / 30.0) * 0.2

        val jitterMs = Random.nextLong(TimeUnit.HOURS.toMillis(4))
        val nextUpdate = now + TimeUnit.HOURS.toMillis(24) + jitterMs

        appFrequencyScoreDao.insertOrUpdate(
            AppFrequencyScoreEntity(
                packageName = packageName,
                score = score,
                lastCalculated = now,
                nextUpdateTime = nextUpdate
            )
        )
    }

    override suspend fun processStaleScores() {
        val now = System.currentTimeMillis()
        val staleEntries = appFrequencyScoreDao.getStaleEntries(now)
        for (entry in staleEntries) {
            recalculateScore(entry.packageName)
        }
    }

    override suspend fun initializeAllScores(packageNames: List<String>) {
        for (packageName in packageNames) {
            val existing = appFrequencyScoreDao.getByPackageName(packageName)
            if (existing == null) {
                recalculateScore(packageName)
            }
        }
    }

    override suspend fun getScoreBreakdown(packageName: String): FrequencyScoreBreakdown {
        val now = System.currentTimeMillis()
        val count3d = appUsageEventDao.countEventsSince(packageName, now - TimeUnit.DAYS.toMillis(3))
        val count10d = appUsageEventDao.countEventsSince(packageName, now - TimeUnit.DAYS.toMillis(10))
        val count30d = appUsageEventDao.countEventsSince(packageName, now - TimeUnit.DAYS.toMillis(30))
        val score = (count3d / 3.0) * 0.5 + (count10d / 10.0) * 0.3 + (count30d / 30.0) * 0.2
        return FrequencyScoreBreakdown(count3d, count10d, count30d, score)
    }
}
