package me.semoro.revy.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.semoro.revy.data.repository.AppUsageRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for launching apps.
 */
@Singleton
class AppLauncherUtils @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appUsageRepository: AppUsageRepository
) {
    private val packageManager = context.packageManager
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Launches an app by its package name.
     *
     * @param packageName The package name of the app to launch
     * @return true if the app was launched successfully, false otherwise
     */
    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                // Record app launch in the database
                coroutineScope.launch {
                    appUsageRepository.recordAppLaunch(packageName)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the app shortcuts for an app.
     *
     * @param packageName The package name of the app
     * @return List of app shortcuts
     */
    fun getAppShortcuts(packageName: String): List<AppShortcut> {
        // This is a simplified implementation that doesn't actually fetch shortcuts
        // In a real implementation, we would use the ShortcutManager API
        return emptyList()
    }

    /**
     * Represents an app shortcut.
     */
    data class AppShortcut(
        val id: String,
        val shortLabel: String,
        val longLabel: String,
        val intent: Intent
    )
}
