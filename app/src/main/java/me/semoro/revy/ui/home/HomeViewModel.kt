package me.semoro.revy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.model.RecencyBucket
import me.semoro.revy.data.repository.AppUsageRepository
import me.semoro.revy.data.repository.PinnedAppsRepository
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
    data class BucketPage(val bucket: RecencyBucket, val apps: List<AppInfo>) : Page()

    /**
     * A page showing search results.
     *
     * @property query The search query
     * @property apps The list of apps matching the search query
     */
    data class SearchPage(val query: String, val apps: List<AppInfo>) : Page()
}

/**
 * ViewModel for the home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    private val pinnedAppsRepository: PinnedAppsRepository
) : ViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Apps by recency bucket
    private val _appsByBucket = MutableStateFlow<Map<RecencyBucket, List<AppInfo>>>(emptyMap())
    val appsByBucket: StateFlow<Map<RecencyBucket, List<AppInfo>>> = _appsByBucket.asStateFlow()

    // Pinned apps
    private val _pinnedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val pinnedApps: StateFlow<List<AppInfo>> = _pinnedApps.asStateFlow()

    // Search state
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    // Pages
    private val _pages = MutableStateFlow<List<Page>>(emptyList())
    val pages: StateFlow<List<Page>> = _pages.asStateFlow()

    init {
        loadApps()

        // Update pages whenever appsByBucket or searchState changes
        viewModelScope.launch {
            combine(_appsByBucket, _searchState) { appsByBucket, searchState ->
                createPages(appsByBucket, searchState)
            }.collect { pages ->
                _pages.value = pages
            }
        }
    }

    /**
     * Creates pages based on apps by bucket and search state.
     *
     * @param appsByBucket Apps grouped by recency bucket
     * @param searchState Current search state
     * @return List of pages
     */
    private fun createPages(
        appsByBucket: Map<RecencyBucket, List<AppInfo>>,
        searchState: SearchState
    ): List<Page> {
        val pages = mutableListOf<Page>()

        // The number of apps per page
        // Regular pages: 4 columns x 7 rows
        val regularAppsPerPage = 28
        // Search pages: 4 columns x 3 rows
        val searchAppsPerPage = 12

        // First, add all regular recency bucket pages
        val regularBuckets = listOf(
            RecencyBucket.TODAY,
            RecencyBucket.THIS_WEEK,
            RecencyBucket.LAST_30_DAYS
        )

        regularBuckets.forEach { bucket ->
            val apps = appsByBucket[bucket] ?: emptyList()
            if (apps.isNotEmpty()) {
                // Split apps into chunks of regularAppsPerPage
                val chunkedApps = apps.chunked(regularAppsPerPage)

                // Add each chunk as a separate page with the same bucket
                chunkedApps.forEach { chunk ->
                    pages.add(Page.BucketPage(bucket, chunk))
                }
            }
        }

        // Add search page
        if (searchState.isActive && searchState.results.isNotEmpty()) {
            // If search is active and we have results, add them to pages
            val searchResults = searchState.results.chunked(searchAppsPerPage)
            searchResults.forEach { chunk ->
                pages.add(Page.SearchPage(searchState.query, chunk))
            }
        } else {
            // Add an empty search page
            pages.add(Page.SearchPage(searchState.query, emptyList()))
        }

        return pages
    }

    /**
     * Loads the apps and updates the UI state.
     */
    private fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            // Get apps by recency bucket with pinned status
            appUsageRepository.getAppsByRecencyBucket().collectLatest { appsByBucket ->
                // Get pinned apps
                val pinnedApps = appsByBucket.values.flatten().filter { it.isPinned }

                // Update UI state
                _appsByBucket.value = appsByBucket
                _pinnedApps.value = pinnedApps
                _isLoading.value = false
            }
        }
    }

    /**
     * Pins or unpins an app.
     *
     * @param packageName The package name of the app to pin or unpin
     * @param pin Whether to pin or unpin the app
     */
    fun togglePinApp(packageName: String, pin: Boolean) {
        viewModelScope.launch {
            if (pin) {
                pinnedAppsRepository.pinApp(packageName)
            } else {
                pinnedAppsRepository.unpinApp(packageName)
            }
        }
    }

    /**
     * Refreshes the app list.
     */
    fun refreshApps() {
        _isLoading.value = true
        loadApps()
    }

    /**
     * Updates the search query and filters apps accordingly.
     *
     * @param query The search query
     */
    fun updateSearchQuery(query: String) {
        val allApps = _appsByBucket.value.values.flatten()

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
