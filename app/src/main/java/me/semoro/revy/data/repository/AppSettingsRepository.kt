package me.semoro.revy.data.repository

import kotlinx.coroutines.flow.Flow
import me.semoro.revy.data.local.room.AppSettingsDao
import me.semoro.revy.data.local.room.AppSettingsEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for accessing app settings data.
 */
interface AppSettingsRepository {
    /**
     * Gets a flow of all app settings.
     *
     * @return Flow of list of AppSettingsEntity objects
     */
    fun getAllAppSettings(): Flow<List<AppSettingsEntity>>

    /**
     * Gets app settings for a specific package.
     *
     * @param packageName The package name of the app
     * @return The app settings entity, or null if not found
     */
    suspend fun getAppSettings(packageName: String): AppSettingsEntity?

    /**
     * Gets app settings for a specific package as a Flow.
     *
     * @param packageName The package name of the app
     * @return Flow of the app settings entity
     */
    fun getAppSettingsFlow(packageName: String): Flow<AppSettingsEntity?>

    /**
     * Sets whether an app should only be shown in search.
     *
     * @param packageName The package name of the app
     * @param showOnlyInSearch Whether the app should only be shown in search
     */
    suspend fun setShowOnlyInSearch(packageName: String, showOnlyInSearch: Boolean)
}

/**
 * Implementation of AppSettingsRepository.
 */
@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    private val appSettingsDao: AppSettingsDao
) : AppSettingsRepository {

    override fun getAllAppSettings(): Flow<List<AppSettingsEntity>> {
        return appSettingsDao.getAllAppSettings()
    }

    override suspend fun getAppSettings(packageName: String): AppSettingsEntity? {
        return appSettingsDao.getAppSettings(packageName)
    }

    override fun getAppSettingsFlow(packageName: String): Flow<AppSettingsEntity?> {
        return appSettingsDao.getAppSettingsFlow(packageName)
    }

    override suspend fun setShowOnlyInSearch(packageName: String, showOnlyInSearch: Boolean) {
        val update = appSettingsDao.updateShowOnlyInSearch(packageName, showOnlyInSearch)
        if (update == 0) {
            // If no rows were updated, insert a new record
            appSettingsDao.insertOrUpdate(AppSettingsEntity(packageName, showOnlyInSearch))
        }
    }
}