package me.semoro.revy.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_frequency_score")
data class AppFrequencyScoreEntity(
    @PrimaryKey
    val packageName: String,
    val score: Double,
    val lastCalculated: Long,
    val nextUpdateTime: Long
)
