package me.semoro.revy.data.model

enum class FrequencyBucket(val title: String, val minScore: Double) {
    DAILY("Daily", 2.0),
    WEEKLY("Weekly", 0.5),
    MONTHLY("Monthly", 0.0);

    companion object {
        fun fromScore(score: Double): FrequencyBucket? {
            if (score <= 0.0) return null
            return entries.firstOrNull { score >= it.minScore }
        }
    }
}
