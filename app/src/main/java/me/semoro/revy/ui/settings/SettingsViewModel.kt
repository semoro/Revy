package me.semoro.revy.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.semoro.revy.data.preferences.LayoutMode
import me.semoro.revy.data.preferences.LayoutModePreferenceManager
import me.semoro.revy.data.repository.AppFrequencyRepository
import me.semoro.revy.data.repository.AppPositioningRepository
import me.semoro.revy.data.repository.AppUsageRepository
import javax.inject.Inject

/**
 * Data class representing the UI state for the Settings screen.
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val statusMessage: String = "",
    val layoutMode: LayoutMode = LayoutMode.RECENCY
)

/**
 * ViewModel for the Settings/Technical screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    private val appPositioningRepository: AppPositioningRepository,
    private val layoutModePreferenceManager: LayoutModePreferenceManager,
    private val appFrequencyRepository: AppFrequencyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            layoutModePreferenceManager.layoutMode.collectLatest { mode ->
                _uiState.update { it.copy(layoutMode = mode) }
            }
        }
    }

    fun setLayoutMode(mode: LayoutMode) {
        viewModelScope.launch {
            layoutModePreferenceManager.setLayoutMode(mode)
            if (mode == LayoutMode.FREQUENCY) {
                // Check if scores are empty and initialize if needed
                val scores = appFrequencyRepository.getAppsByFrequency().first()
                if (scores.isEmpty()) {
                    val allApps = appUsageRepository.getAppsWithUsageInfo().first()
                    appFrequencyRepository.initializeAllScores(allApps.map { it.packageName })
                }
            }
        }
    }

    /**
     * Rescans usage events to update app usage data.
     */
    fun rescanUsageEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Rescanning usage events...") }
            try {
                appUsageRepository.checkAppUsageActivity(rescan = true)
                _uiState.update { it.copy(isLoading = false, statusMessage = "Usage events rescanned successfully") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Error rescanning usage events: ${e.message}") }
            }
        }
    }

    /**
     * Packs apps by removing all gravestones and rearranging apps to fill in the empty slots.
     */
    fun packApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Packing apps...") }
            try {
                val gravestonesRemoved = appPositioningRepository.packApps()
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        statusMessage = "Apps packed successfully. Removed $gravestonesRemoved gravestones."
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Error packing apps: ${e.message}") }
            }
        }
    }

    fun reloadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Reloading apps...") }
            appUsageRepository.reloadApps()
            _uiState.update { it.copy(isLoading = false, statusMessage = "Apps reloaded") }
        }
    }
}