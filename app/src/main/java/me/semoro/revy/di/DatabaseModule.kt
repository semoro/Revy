package me.semoro.revy.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.semoro.revy.data.local.room.AppDatabase
import me.semoro.revy.data.local.room.AppUsageDao
import javax.inject.Singleton

/**
 * Hilt module for providing database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides the Room database instance.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    /**
     * Provides the AppUsageDao.
     */
    @Provides
    @Singleton
    fun provideAppUsageDao(appDatabase: AppDatabase): AppUsageDao {
        return appDatabase.appUsageDao()
    }
}