package me.semoro.revy.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import me.semoro.revy.data.local.PinnedAppsDataStore
import me.semoro.revy.data.model.AppInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for managing pinned apps.
 */
interface PinnedAppsRepository {
    /**
     * Gets a flow of the set of pinned app package names.
     *
     * @return Flow of set of pinned app package names
     */
    val pinnedApps: Flow<Set<String>>
    
    /**
     * Gets a flow of the ordered list of pinned app package names.
     *
     * @return Flow of list of pinned app package names in order
     */
    val pinnedAppsOrder: Flow<List<String>>
    
    /**
     * Pins an app.
     *
     * @param packageName The package name of the app to pin
     */
    suspend fun pinApp(packageName: String)
    
    /**
     * Unpins an app.
     *
     * @param packageName The package name of the app to unpin
     */
    suspend fun unpinApp(packageName: String)
    
    /**
     * Updates the order of pinned apps.
     *
     * @param orderedPackageNames The ordered list of pinned app package names
     */
    suspend fun updatePinnedAppsOrder(orderedPackageNames: List<String>)
    
    /**
     * Combines app info with pinned status.
     *
     * @param apps The list of app info objects
     * @return Flow of list of app info objects with updated pinned status
     */
    fun combineWithPinnedStatus(apps: Flow<List<AppInfo>>): Flow<List<AppInfo>>
}

/**
 * Implementation of PinnedAppsRepository.
 */
@Singleton
class PinnedAppsRepositoryImpl @Inject constructor(
    private val pinnedAppsDataStore: PinnedAppsDataStore
) : PinnedAppsRepository {
    
    override val pinnedApps: Flow<Set<String>> = pinnedAppsDataStore.pinnedApps
    
    override val pinnedAppsOrder: Flow<List<String>> = pinnedAppsDataStore.pinnedAppsOrder
    
    override suspend fun pinApp(packageName: String) {
        pinnedAppsDataStore.pinApp(packageName)
    }
    
    override suspend fun unpinApp(packageName: String) {
        pinnedAppsDataStore.unpinApp(packageName)
    }
    
    override suspend fun updatePinnedAppsOrder(orderedPackageNames: List<String>) {
        pinnedAppsDataStore.updatePinnedAppsOrder(orderedPackageNames)
    }
    
    override fun combineWithPinnedStatus(apps: Flow<List<AppInfo>>): Flow<List<AppInfo>> {
        return apps.combine(pinnedApps) { appsList, pinnedSet ->
            appsList.map { app ->
                app.copy(isPinned = pinnedSet.contains(app.packageName))
            }
        }
    }
}