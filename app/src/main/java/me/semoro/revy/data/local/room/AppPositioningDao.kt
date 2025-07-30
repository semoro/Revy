package me.semoro.revy.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the app_positioning table.
 */
@Dao
interface AppPositioningDao {
    /**
     * Insert or update multiple app positioning records.
     *
     * @param appPositionings The app positioning entities to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(appPositionings: List<AppPositioningEntity>)

    /**
     * Get all app positioning records for a specific key.
     *
     * @param key The key to query
     * @return Flow of list of all app positioning entities for the specified key, ordered by position
     */
    @Query("SELECT * FROM app_positioning WHERE `key` = :key ORDER BY position")
    suspend fun getAppPositioningsByKey(key: String): List<AppPositioningEntity>

    /**
     * Delete all app positioning records for a specific key.
     *
     * @param key The key to delete records for
     */
    @Query("DELETE FROM app_positioning WHERE `key` = :key")
    suspend fun deleteByKey(key: String)

    /**
     * Delete app positioning records for a specific key and position greater than or equal to a value.
     *
     * @param key The key to delete records for
     * @param position The position threshold
     */
    @Query("DELETE FROM app_positioning WHERE `key` = :key AND packageName NOT IN (:nameList)")
    suspend fun deleteAbsent(key: String, nameList: List<String>)
}
