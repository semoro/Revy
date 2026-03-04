package me.semoro.revy.data.local.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_usage_event",
    indices = [Index("openTimestamp"), Index("packageName")]
)
data class AppUsageEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val openTimestamp: Long
)