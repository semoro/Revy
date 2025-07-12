package me.semoro.revy.util

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for handling permissions.
 */
@Singleton
class PermissionUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Checks if the app has permission to access usage statistics.
     *
     * @return true if the app has permission, false otherwise
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Checks if all required permissions are granted.
     *
     * @return true if all permissions are granted, false otherwise
     */
    fun hasAllRequiredPermissions(): Boolean {
        return hasUsageStatsPermission()
    }
}
