package me.semoro.revy.ui.permissions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.semoro.revy.data.repository.AppUsageRepository
import javax.inject.Inject

/**
 * ViewModel for the permission screen.
 */
@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository
) : ViewModel() {
    
    /**
     * Checks if the app has permission to access usage statistics.
     *
     * @return true if the app has permission, false otherwise
     */
    fun hasUsageStatsPermission(): Boolean {
        return appUsageRepository.hasUsageStatsPermission()
    }
}