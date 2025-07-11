package me.semoro.revy.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore for managing pinned apps.
 */
@Singleton
class PinnedAppsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pinned_apps")
    
    private val pinnedAppsKey = stringPreferencesKey("pinned_apps")
    private val pinnedAppsOrderKey = stringPreferencesKey("pinned_apps_order")
    
    /**
     * Gets a flow of the list of pinned app package names.
     *
     * @return Flow of set of pinned app package names
     */
    val pinnedApps: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[pinnedAppsKey]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }
    
    /**
     * Gets a flow of the ordered list of pinned app package names.
     *
     * @return Flow of list of pinned app package names in order
     */
    val pinnedAppsOrder: Flow<List<String>> = context.dataStore.data.map { preferences ->
        preferences[pinnedAppsOrderKey]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }
    
    /**
     * Pins an app.
     *
     * @param packageName The package name of the app to pin
     */
    suspend fun pinApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentPinnedApps = preferences[pinnedAppsKey]?.split(",")?.filter { it.isNotEmpty() }?.toMutableSet() ?: mutableSetOf()
            currentPinnedApps.add(packageName)
            preferences[pinnedAppsKey] = currentPinnedApps.joinToString(",")
            
            // Also update the order
            val currentOrder = preferences[pinnedAppsOrderKey]?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
            if (!currentOrder.contains(packageName)) {
                currentOrder.add(packageName)
                preferences[pinnedAppsOrderKey] = currentOrder.joinToString(",")
            }
        }
    }
    
    /**
     * Unpins an app.
     *
     * @param packageName The package name of the app to unpin
     */
    suspend fun unpinApp(packageName: String) {
        context.dataStore.edit { preferences ->
            val currentPinnedApps = preferences[pinnedAppsKey]?.split(",")?.filter { it.isNotEmpty() }?.toMutableSet() ?: mutableSetOf()
            currentPinnedApps.remove(packageName)
            preferences[pinnedAppsKey] = currentPinnedApps.joinToString(",")
            
            // Also update the order
            val currentOrder = preferences[pinnedAppsOrderKey]?.split(",")?.filter { it.isNotEmpty() }?.toMutableList() ?: mutableListOf()
            currentOrder.remove(packageName)
            preferences[pinnedAppsOrderKey] = currentOrder.joinToString(",")
        }
    }
    
    /**
     * Updates the order of pinned apps.
     *
     * @param orderedPackageNames The ordered list of pinned app package names
     */
    suspend fun updatePinnedAppsOrder(orderedPackageNames: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[pinnedAppsOrderKey] = orderedPackageNames.joinToString(",")
            
            // Also update the set of pinned apps
            preferences[pinnedAppsKey] = orderedPackageNames.joinToString(",")
        }
    }
}