package me.semoro.revy.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing app usage data.
 *
 * @property packageName The package name of the application (primary key)
 * @property lastUsedTimestamp The timestamp when the application was last used
 */
@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey
    val packageName: String,
    val lastUsedTimestamp: Long
)