package me.semoro.revy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.model.RecencyBucket
import me.semoro.revy.data.repository.AppUsageRepository
import me.semoro.revy.data.repository.PinnedAppsRepository
import javax.inject.Inject

/**
 * ViewModel for the home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    private val pinnedAppsRepository: PinnedAppsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadApps()
    }
    
    /**
     * Loads the apps and updates the UI state.
     */
    private fun loadApps() {
        viewModelScope.launch {
            // Get apps by recency bucket with pinned status
            appUsageRepository.getAppsByRecencyBucket().collectLatest { appsByBucket ->
                // Get pinned apps
                val pinnedApps = appsByBucket.values.flatten().filter { it.isPinned }
                
                // Update UI state
                _uiState.value = HomeUiState(
                    isLoading = false,
                    pinnedApps = pinnedApps,
                    appsByBucket = appsByBucket
                )
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
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadApps()
    }
}

/**
 * UI state for the home screen.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val pinnedApps: List<AppInfo> = emptyList(),
    val appsByBucket: Map<RecencyBucket, List<AppInfo>> = emptyMap()
)