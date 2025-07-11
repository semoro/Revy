package me.semoro.revy.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
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
     * Creates an intent to open the usage access settings screen.
     *
     * @return Intent to open the usage access settings
     */
    fun createUsageAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    /**
     * Checks if the app has permission to delete packages.
     * 
     * Note: We don't actually need to check this permission as the system will prompt
     * the user for confirmation when we try to uninstall an app.
     *
     * @return Always returns true as the system handles the permission check
     */
    fun hasDeletePackagesPermission(): Boolean {
        // The REQUEST_DELETE_PACKAGES permission is automatically granted
        // The system will show a confirmation dialog when we try to uninstall an app
        return true
    }

    /**
     * Creates an intent to uninstall an app.
     *
     * @param packageName The package name of the app to uninstall
     * @return Intent to uninstall the app
     */
    fun createUninstallAppIntent(packageName: String): Intent {
        return Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
            data = android.net.Uri.parse("package:$packageName")
        }
    }

    /**
     * Creates an intent to open the app info screen.
     *
     * @param packageName The package name of the app
     * @return Intent to open the app info screen
     */
    fun createAppInfoIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:$packageName")
        }
    }
}
