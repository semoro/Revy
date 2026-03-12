package me.semoro.revy.data.preferences

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

enum class LayoutMode {
    RECENCY,
    FREQUENCY
}

private val Context.layoutDataStore: DataStore<Preferences> by preferencesDataStore(name = "layout_preferences")

@Singleton
class LayoutModePreferenceManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val layoutModeKey = stringPreferencesKey("layout_mode")

    val layoutMode: Flow<LayoutMode> = context.layoutDataStore.data.map { preferences ->
        val name = preferences[layoutModeKey] ?: LayoutMode.RECENCY.name
        LayoutMode.valueOf(name)
    }

    suspend fun setLayoutMode(mode: LayoutMode) {
        context.layoutDataStore.edit { preferences ->
            preferences[layoutModeKey] = mode.name
        }
    }
}
