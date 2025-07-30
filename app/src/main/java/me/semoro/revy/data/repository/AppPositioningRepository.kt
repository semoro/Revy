package me.semoro.revy.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.semoro.revy.data.local.room.AppPositioningDao
import me.semoro.revy.data.local.room.AppPositioningEntity
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.model.SlotInfo
import javax.inject.Inject
import javax.inject.Singleton

interface AppPositioningRepository {
    fun positionIntoSlots(key: String, apps: List<AppInfo>): List<SlotInfo>
}


@Singleton
class AppPositioningRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appPositioningDao: AppPositioningDao
) : AppPositioningRepository {

    /**
     * Positions the given app info list according to the stored slots
     * E.g
     * let's say there is a new element added in the list -> it should take the position of the first gravestone
     * or a new slot should be added in the end
     */
    override fun positionIntoSlots(
        key: String,
        apps: List<AppInfo>
    ): List<SlotInfo> = runBlocking {
        // Get the current positioning for this key
        val currentPositioning = appPositioningDao.getAppPositioningsByKeySync(key)

        // Create a map of package name to app info for quick lookup
        val appsByPackage = apps.associateBy { it.packageName }

        // Create a list to hold the result
        val result = mutableListOf<SlotInfo>()

        // If we have existing positioning data, use it to position the apps
        if (currentPositioning.isNotEmpty()) {
            // Create a set of package names that have been positioned
            val positionedPackages = mutableSetOf<String>()

            // First, add all positioned apps and gravestones in their correct positions
            for (position in currentPositioning) {
                if (position.isGravestone) {
                    // Add a gravestone
                    result.add(SlotInfo.Gravestone)
                } else {
                    // Try to find the app in the provided list
                    val app = appsByPackage[position.packageName]
                    if (app != null) {
                        // Add the app to the result
                        result.add(app)
                        positionedPackages.add(position.packageName)
                    } else {
                        // App no longer exists, add a gravestone instead
                        result.add(SlotInfo.Gravestone)
                    }
                }
            }

            // Add any new apps that weren't in the positioning data
            val newApps = apps.filter { it.packageName !in positionedPackages }

            // Try to fill gravestones with new apps
            var newAppIndex = 0
            for (i in result.indices) {
                if (result[i] == SlotInfo.Gravestone && newAppIndex < newApps.size) {
                    result[i] = newApps[newAppIndex]
                    newAppIndex++
                }
            }

            // If there are still new apps left, add them to the end
            if (newAppIndex < newApps.size) {
                result.addAll(newApps.subList(newAppIndex, newApps.size))
            }
        } else {
            // No existing positioning data, just use the apps as they are
            result.addAll(apps)
        }

        // Save the new positioning
        val newPositioning = result.mapIndexed { index, slotInfo ->
            when (slotInfo) {
                is AppInfo -> AppPositioningEntity(
                    key = key,
                    packageName = slotInfo.packageName,
                    position = index,
                    isGravestone = false
                )
                SlotInfo.Gravestone -> AppPositioningEntity(
                    key = key,
                    packageName = "",
                    position = index,
                    isGravestone = true
                )
            }
        }

        // Update the database with the new positioning
        appPositioningDao.replaceByKey(key, newPositioning)

        // Return the result
        result
    }
}
