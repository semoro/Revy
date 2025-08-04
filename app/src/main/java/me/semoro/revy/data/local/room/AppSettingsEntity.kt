package me.semoro.revy.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing app-specific settings.
 *
 * @property packageName The package name of the application (primary key)
 * @property showOnlyInSearch Whether the app should only be shown in search results
 */
@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val packageName: String,
    val showOnlyInSearch: Boolean = false
)