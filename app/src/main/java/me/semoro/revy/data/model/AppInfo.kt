package me.semoro.revy.data.model

import android.graphics.Bitmap

/**
 * Represents information about an installed application.
 *
 * @property packageName The package name of the application
 * @property label The user-visible name of the application
 * @property icon The application icon
 * @property lastUsedTimestamp The timestamp when the application was last used
 * @property isPinned Whether the application is pinned to the top
 */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Bitmap,
    val lastUsedTimestamp: Long,
    val isPinned: Boolean = false
) : SlotInfo()

sealed class SlotInfo {
    object Gravestone: SlotInfo()
}