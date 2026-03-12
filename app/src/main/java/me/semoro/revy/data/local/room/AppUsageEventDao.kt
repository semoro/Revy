package me.semoro.revy.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(appUsageEventEntity: List<AppUsageEventEntity>)

    @Query("DELETE FROM app_usage_event WHERE openTimestamp >= :since AND openTimestamp <= :till")
    suspend fun removeAllEventsInRange(since: Long, till: Long)

    @Query("DELETE FROM app_usage_event WHERE openTimestamp <= :till")
    suspend fun cleanUpOutdatedEvents(till: Long)


    @Query("SELECT * FROM app_usage_event WHERE packageName=:packageName AND openTimestamp >= :sinceTime")
    suspend fun getRecentEvents(packageName: String, sinceTime: Long): List<AppUsageEventEntity>

    @Query("SELECT COUNT(*) FROM app_usage_event WHERE packageName = :packageName AND openTimestamp >= :sinceTime")
    suspend fun countEventsSince(packageName: String, sinceTime: Long): Int
}