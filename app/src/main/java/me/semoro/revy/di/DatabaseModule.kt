package me.semoro.revy.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.semoro.revy.data.local.room.AppDatabase
import me.semoro.revy.data.local.room.AppPositioningDao
import me.semoro.revy.data.local.room.AppUsageDao
import me.semoro.revy.data.repository.AppPositioningRepository
import me.semoro.revy.data.repository.AppPositioningRepositoryImpl
import me.semoro.revy.data.repository.AppUsageRepository
import me.semoro.revy.data.repository.AppUsageRepositoryImpl
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

    /**
     * Provides the AppUsageRepository implementation.
     */
    @Provides
    @Singleton
    fun provideAppUsageRepository(
        @ApplicationContext context: Context,
        appUsageDao: AppUsageDao
    ): AppUsageRepository {
        return AppUsageRepositoryImpl(context, appUsageDao)
    }

    /**
     * Provides the AppPositioningDao.
     */
    @Provides
    @Singleton
    fun provideAppPositioningDao(appDatabase: AppDatabase): AppPositioningDao {
        return appDatabase.appPositioningDao()
    }

    /**
     * Provides the AppPositioningRepository implementation.
     */
    @Provides
    @Singleton
    fun provideAppPositioningRepository(
        appPositioningDao: AppPositioningDao,
        appDatabase: AppDatabase
    ): AppPositioningRepository {
        return AppPositioningRepositoryImpl(appPositioningDao, appDatabase)
    }
}
