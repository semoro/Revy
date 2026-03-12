package me.semoro.revy.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.model.FrequencyBucket
import me.semoro.revy.data.model.RecencyBucket
import me.semoro.revy.data.model.SlotInfo
import me.semoro.revy.data.preferences.LayoutMode
import me.semoro.revy.data.preferences.LayoutModePreferenceManager
import me.semoro.revy.data.repository.AppFrequencyRepository
import me.semoro.revy.data.repository.AppPositioningRepository
import me.semoro.revy.data.repository.AppSettingsRepository
import me.semoro.revy.data.repository.AppUsageRepository
import me.semoro.revy.util.AppLauncherUtils
import javax.inject.Inject

/**
 * Sealed class representing a page in the home screen.
 */
sealed class Page {
    /**
     * A page showing apps from a recency bucket.
     *
     * @property bucket The recency bucket
     * @property apps The list of apps in this page
     */
    data class BucketPage(val bucket: RecencyBucket, val apps: List<SlotInfo>) : Page()

    /**
     * A page showing apps ranked by usage frequency (flat).
     *
     * @property apps The list of apps in this page
     */
    data class FrequencyPage(val apps: List<SlotInfo>) : Page()

    /**
     * A page showing apps from a frequency bucket (daily/weekly/monthly).
     *
     * @property bucket The frequency bucket
     * @property apps The list of apps in this page
     */
    data class FrequencyBucketPage(val bucket: FrequencyBucket, val apps: List<SlotInfo>) : Page()

    /**
     * A page showing search results.
     *
     * @property query The search query
     * @property apps The list of apps matching the search query
     */
    data class SearchPage(val query: String, val apps: List<AppInfo>) : Page()
}

context(vm: ViewModel)
fun <T> compute(body: @Composable () -> T,) = vm.viewModelScope.launchMolecule(RecompositionMode.Immediate, body = body)

/**
 * ViewModel for the home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    private val appPositioningRepository: AppPositioningRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val layoutModePreferenceManager: LayoutModePreferenceManager,
    private val appFrequencyRepository: AppFrequencyRepository,
    val appLauncherUtils: AppLauncherUtils
) : ViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Apps by recency bucket
    private val _appsWithUsageInfo = MutableStateFlow<List<AppInfo>>(emptyList())

    // Search state
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Pages
    val pages: StateFlow<List<Page>> = compute { createPages() }

    init {
        viewModelScope.launch {
            _isLoading.value = true
            // Get apps by recency bucket with pinned status
            appUsageRepository.getAppsWithUsageInfo().collectLatest { appsByBucket ->
                // Update UI state
                _appsWithUsageInfo.value = appsByBucket
                _isLoading.value = false
            }
        }
    }

    /**
     * Creates pages based on apps by bucket and search state.
     *
     * @return List of pages
     */
    @Composable
    private fun createPages(): List<Page> {
        val appsWithUsageInfo by _appsWithUsageInfo.collectAsState()
        val appSettings by appSettingsRepository.getAllAppSettings().collectAsState(emptyList())
        val layoutMode by layoutModePreferenceManager.layoutMode.collectAsState(LayoutMode.RECENCY)
        val frequencyScores by appFrequencyRepository.getAppsByFrequency().collectAsState(emptyList())

        val appSettingsByPackage = appSettings.associateBy { it.packageName }
        val searchState by searchState.collectAsState()
        val pages = mutableListOf<Page>()

        // The number of apps per page
        // Regular pages: 4 columns x 7 rows
        val regularAppsPerPage = 28
        // Search pages: 4 columns x 3 rows
        val searchAppsPerPage = 12

        // Filter out apps that should only be shown in search
        val filteredApps = appsWithUsageInfo.filter { app ->
            appSettingsByPackage[app.packageName]?.showOnlyInSearch != true
        }

        val packingGeneration by appPositioningRepository.packingGeneration.collectAsState()

        when (layoutMode) {
            LayoutMode.RECENCY -> {
                val regularBuckets = listOf(
                    RecencyBucket.TODAY,
                    RecencyBucket.THIS_WEEK,
                    RecencyBucket.LAST_30_DAYS
                )

                val appsByBucket = filteredApps.groupBy {
                    RecencyBucket.fromTimestamp(it.lastUsedTimestamp)
                }

                regularBuckets.forEach { bucket ->
                    val apps = appsByBucket[bucket] ?: emptyList()
                    if (apps.isNotEmpty()) {
                        val positioned = remember(bucket.name, packingGeneration, apps) {
                            appPositioningRepository.positionIntoSlots(bucket.name, apps)
                        }

                        val chunkedApps = positioned.chunked(regularAppsPerPage)
                        chunkedApps.forEach { chunk ->
                            pages.add(Page.BucketPage(bucket, chunk))
                        }
                    }
                }
            }

            LayoutMode.FREQUENCY -> {
                val scoresByPackage = frequencyScores.associate { it.packageName to it.score }
                // Skip apps with zero score (no uses in last 30 days)
                val sortedApps = filteredApps
                    .filter { (scoresByPackage[it.packageName] ?: 0.0) > 0.0 }
                    .sortedByDescending { scoresByPackage[it.packageName] ?: 0.0 }

                val positioned = remember("FREQUENCY", packingGeneration, sortedApps) {
                    appPositioningRepository.positionIntoSlots("FREQUENCY", sortedApps)
                }

                // Cap at 3 pages max (top 84 apps)
                val chunkedApps = positioned.chunked(regularAppsPerPage).take(3)
                chunkedApps.forEach { chunk ->
                    pages.add(Page.FrequencyPage(chunk))
                }
            }

            LayoutMode.FREQUENCY_BUCKETED -> {
                val scoresByPackage = frequencyScores.associate { it.packageName to it.score }

                val appsByBucket = filteredApps
                    .mapNotNull { app ->
                        val score = scoresByPackage[app.packageName] ?: 0.0
                        val bucket = FrequencyBucket.fromScore(score)
                        bucket?.let { it to app }
                    }
                    .groupBy({ it.first }, { it.second })

                FrequencyBucket.entries.forEach { bucket ->
                    val apps = appsByBucket[bucket] ?: emptyList()
                    if (apps.isNotEmpty()) {
                        val key = "FREQ_${bucket.name}"
                        val sortedApps = apps.sortedByDescending { scoresByPackage[it.packageName] ?: 0.0 }
                        val positioned = remember(key, packingGeneration, sortedApps) {
                            appPositioningRepository.positionIntoSlots(key, sortedApps)
                        }

                        val chunkedApps = positioned.chunked(regularAppsPerPage)
                        chunkedApps.forEach { chunk ->
                            pages.add(Page.FrequencyBucketPage(bucket, chunk))
                        }
                    }
                }
            }
        }

        // Add search page
        if (searchState.isActive && searchState.results.isNotEmpty()) {
            val searchResults = searchState.results.chunked(searchAppsPerPage)
            searchResults.forEach { chunk ->
                pages.add(Page.SearchPage(searchState.query, chunk))
            }
        } else {
            pages.add(Page.SearchPage(searchState.query, emptyList()))
        }

        return pages
    }

    /**
     * Updates the search query and filters apps accordingly.
     *
     * @param query The search query
     */
    fun updateSearchQuery(query: String) {
        val allApps = _appsWithUsageInfo.value

        val filteredApps = if (query.isBlank()) {
            emptyList()
        } else {
            allApps.filter { 
                it.label.contains(query, ignoreCase = true) 
            }
        }

        _searchState.value = _searchState.value.copy(
            query = query,
            results = filteredApps
        )
    }

    /**
     * Activates or deactivates search mode.
     *
     * @param active Whether search mode should be active
     */
    fun setSearchActive(active: Boolean) {
        // If deactivating search, clear the search query and results
        if (!active) {
            _searchState.value = SearchState()
        } else {
            _searchState.value = _searchState.value.copy(
                isActive = true
            )
        }
    }
}

/**
 * Search state for the home screen.
 */
data class SearchState(
    val query: String = "",
    val isActive: Boolean = false,
    val results: List<AppInfo> = emptyList()
)
