package me.semoro.revy.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for the application.
 */
@Database(entities = [AppUsageEntity::class, AppPositioningEntity::class], version = 2, exportSchema = false)
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
