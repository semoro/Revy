package me.semoro.revy.ui.permissions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.semoro.revy.data.repository.AppUsageRepository
import me.semoro.revy.util.PermissionUtils
import javax.inject.Inject

/**
 * ViewModel for the permission screen.
 */
@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    private val permissionUtils: PermissionUtils
) : ViewModel() {


    /**
     * Checks if the app has all required permissions.
     *
     * @return true if all required permissions are granted, false otherwise
     */
    fun hasAllRequiredPermissions(): Boolean {
        return permissionUtils.hasAllRequiredPermissions()
    }
}
