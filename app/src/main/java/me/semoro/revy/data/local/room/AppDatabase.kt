package me.semoro.revy.data.local.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for the application.
 */
@Database(
    entities = [AppUsageEntity::class, AppPositioningEntity::class, AppSettingsEntity::class, AppUsageEventEntity::class, AppFrequencyScoreEntity::class],
    autoMigrations = [
        AutoMigration(
            from = 5,
            to = 6
        ),
        AutoMigration(
            from = 6,
            to = 7
        )
    ],
    version = 7, exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Get the DAO for app usage.
     *
     * @return The app usage DAO
     */
    abstract fun appUsageDao(): AppUsageDao

    /**
     * Get the DAO for app positioning.
     *
     * @return The app positioning DAO
     */
    abstract fun appPositioningDao(): AppPositioningDao

    /**
     * Get the DAO for app settings.
     *
     * @return The app settings DAO
     */
    abstract fun appSettingsDao(): AppSettingsDao

    abstract fun appUsageEventDao(): AppUsageEventDao

    abstract fun appFrequencyScoreDao(): AppFrequencyScoreDao

    companion object {
        private const val DATABASE_NAME = "revy_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get the singleton instance of the database.
         *
         * @param context The application context
         * @return The database instance
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
