package me.semoro.revy.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import me.semoro.revy.data.local.room.AppDatabase
import me.semoro.revy.data.local.room.AppPositioningDao
import me.semoro.revy.data.local.room.AppPositioningEntity
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.model.SlotInfo
import javax.inject.Inject
import javax.inject.Singleton

interface AppPositioningRepository {
    fun positionIntoSlots(key: String, apps: List<AppInfo>): List<SlotInfo>


    val packingGeneration: StateFlow<Int>
    /**
     * Packs apps by removing all gravestones and rearranging apps to fill in the empty slots.
     * 
     * @return The number of gravestones removed
     */
    suspend fun packApps(): Int
}


@Singleton
class AppPositioningRepositoryImpl @Inject constructor(
    private val appPositioningDao: AppPositioningDao,
    private val db: AppDatabase
) : AppPositioningRepository {

    /**
     * Packs apps by removing all gravestones and rearranging apps to fill in the empty slots.
     * 
     * @return The number of gravestones removed
     */
    override suspend fun packApps(): Int = db.withTransaction {

        var totalRemoved = 0
        for (key in appPositioningDao.getAllKeys()) {
            val positions = appPositioningDao.getAppPositioningsByKey(key)

            // Find all positions with apps (non-gravestones)
            val nonGravestonePositions = positions.sortedBy { it.position }

            println(positions)
            val totalSlots = positions.maxOfOrNull { it.position }?.let { it + 1 } ?: 0
            // Count how many gravestones will be removed
            totalRemoved += totalSlots - nonGravestonePositions.size

            // Create new positions without gravestones
            val newPositions = nonGravestonePositions.mapIndexed { index, entity ->
                AppPositioningEntity(
                    key = entity.key,
                    packageName = entity.packageName,
                    position = index
                )
            }

            // Delete all existing positions for this key
            appPositioningDao.deleteByKey(key)

            // Insert the new positions
            appPositioningDao.insertOrUpdateAll(newPositions)

        }
        _packingGeneration.update { it + 1 }
        totalRemoved
    }

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
        // Create a map of package name to app info for quick lookup
        val appsByPackage = apps.associateBy { it.packageName }.toMutableMap()

        db.withTransaction {
            // delete absents
            appPositioningDao.deleteAbsent(key, apps.map { it.packageName })
            val positions = appPositioningDao.getAppPositioningsByKey(key)

            // Create a list to hold the result
            val top = positions.maxOfOrNull { it.position } ?: 0
            val result = MutableList<SlotInfo>(top + 1) {
                SlotInfo.Gravestone
            }
            for (position in positions) {
                result[position.position] =
                    appsByPackage.remove(position.packageName)!! // Bind existing
            }
            val remaining = appsByPackage.values.toList()
            val toSave = mutableListOf<AppPositioningEntity>()

            fun insertOrAppend(toAdd: AppInfo) {
                for ((idx, element) in result.withIndex()) {
                    if (element == SlotInfo.Gravestone) {
                        result[idx] = toAdd
                        toSave.add(
                            AppPositioningEntity(
                                key = key,
                                packageName = toAdd.packageName,
                                position = idx
                            )
                        )
                        return
                    }
                }
                val pos = result.size
                result.add(toAdd)
                toSave.add(
                    AppPositioningEntity(
                        key = key,
                        packageName = toAdd.packageName,
                        position = pos
                    )
                )
            }
            for (it in remaining) {
                insertOrAppend(it)
            }
            appPositioningDao.insertOrUpdateAll(toSave)

            result
        }

    }

    private val _packingGeneration = MutableStateFlow(0)
    override val packingGeneration: StateFlow<Int> = _packingGeneration.asStateFlow()
}
