package me.semoro.revy.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the app_settings table.
 */
@Dao
interface AppSettingsDao {
    /**
     * Insert or update an app settings record.
     *
     * @param appSettings The app settings entity to insert or update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(appSettings: AppSettingsEntity)

    /**
     * Get all app settings records.
     *
     * @return Flow of list of all app settings entities
     */
    @Query("SELECT * FROM app_settings")
    fun getAllAppSettings(): Flow<List<AppSettingsEntity>>

    /**
     * Get app settings record for a specific package.
     *
     * @param packageName The package name to query
     * @return The app settings entity for the specified package, or null if not found
     */
    @Query("SELECT * FROM app_settings WHERE packageName = :packageName")
    suspend fun getAppSettings(packageName: String): AppSettingsEntity?

    /**
     * Get app settings record for a specific package as a Flow.
     *
     * @param packageName The package name to query
     * @return Flow of the app settings entity for the specified package
     */
    @Query("SELECT * FROM app_settings WHERE packageName = :packageName")
    fun getAppSettingsFlow(packageName: String): Flow<AppSettingsEntity?>

    /**
     * Update the showOnlyInSearch setting for an app.
     *
     * @param packageName The package name of the app
     * @param showOnlyInSearch The new showOnlyInSearch value
     * @return The number of rows updated
     */
    @Query("UPDATE app_settings SET showOnlyInSearch = :showOnlyInSearch WHERE packageName = :packageName")
    suspend fun updateShowOnlyInSearch(packageName: String, showOnlyInSearch: Boolean): Int

    /**
     * Delete app settings record for a specific package.
     *
     * @param packageName The package name to delete
     */
    @Query("DELETE FROM app_settings WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}