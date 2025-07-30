package me.semoro.revy.data.local.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity for storing app positioning data.
 *
 * @property id The primary key (auto-generated)
 * @property key The key identifying a specific set of slots
 * @property packageName The package name of the application
 * @property position The position of the app in the slot layout
 * @property isGravestone Whether this slot is a gravestone (empty slot)
 */
@Entity(tableName = "app_positioning",
    indices = [Index("key"), Index("packageName")]
)
data class AppPositioningEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val packageName: String,
    val position: Int,
)