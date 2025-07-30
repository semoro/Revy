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
     * Insert or update an app positioning record.
     *
     * @param appPositioning The app positioning entity to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(appPositioning: AppPositioningEntity)

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
    fun getAppPositioningsByKey(key: String): Flow<List<AppPositioningEntity>>

    /**
     * Get all app positioning records for a specific key.
     *
     * @param key The key to query
     * @return List of all app positioning entities for the specified key, ordered by position
     */
    @Query("SELECT * FROM app_positioning WHERE `key` = :key ORDER BY position")
    suspend fun getAppPositioningsByKeySync(key: String): List<AppPositioningEntity>

    /**
     * Delete all app positioning records for a specific key.
     *
     * @param key The key to delete records for
     */
    @Query("DELETE FROM app_positioning WHERE `key` = :key")
    suspend fun deleteByKey(key: String)

    /**
     * Replace all app positioning records for a specific key.
     *
     * @param key The key to replace records for
     * @param appPositionings The new app positioning entities
     */
    @Transaction
    suspend fun replaceByKey(key: String, appPositionings: List<AppPositioningEntity>) {
        deleteByKey(key)
        insertOrUpdateAll(appPositionings)
    }
}