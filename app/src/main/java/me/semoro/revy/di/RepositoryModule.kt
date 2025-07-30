package me.semoro.revy.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.semoro.revy.data.local.AppUsageDataSource
import me.semoro.revy.data.local.AppUsageDataSourceImpl
import me.semoro.revy.data.repository.AppUsageRepository
import me.semoro.revy.data.repository.AppUsageRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Binds the AppUsageDataSource implementation to the interface.
     */
    @Binds
    @Singleton
    abstract fun bindAppUsageDataSource(
        appUsageDataSourceImpl: AppUsageDataSourceImpl
    ): AppUsageDataSource
    
    /**
     * Binds the AppUsageRepository implementation to the interface.
     */
    @Binds
    @Singleton
    abstract fun bindAppUsageRepository(
        appUsageRepositoryImpl: AppUsageRepositoryImpl
    ): AppUsageRepository

}