package me.semoro.revy.data.model

import java.util.concurrent.TimeUnit

/**
 * Represents different time-based buckets for sorting apps by recency.
 *
 * @property title The user-visible title of the bucket
 * @property startTime The start time of the bucket in milliseconds from now (negative value)
 * @property endTime The end time of the bucket in milliseconds from now (negative value)
 */
enum class RecencyBucket(val title: String, val startTime: Long, val endTime: Long) {
    TODAY("Today", -TimeUnit.HOURS.toMillis(24), 0),
    THIS_WEEK("This week", -TimeUnit.DAYS.toMillis(7), -TimeUnit.HOURS.toMillis(24)),
    LAST_30_DAYS("30 days", -TimeUnit.DAYS.toMillis(30), -TimeUnit.HOURS.toDays(7)),
    OLDER("Older", Long.MIN_VALUE, -TimeUnit.DAYS.toMillis(7));

    companion object {
        /**
         * Determines the appropriate bucket for a timestamp.
         *
         * @param timestamp The timestamp to check
         * @return The RecencyBucket that contains the timestamp
         */
        fun fromTimestamp(timestamp: Long): RecencyBucket {
            val now = System.currentTimeMillis()
            val delta = timestamp - now
            
            return values().firstOrNull { bucket ->
                delta in bucket.startTime..bucket.endTime
            } ?: OLDER
        }
    }
}