package me.semoro.revy.ui.appsettings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.repository.AppUsageRepository
import me.semoro.revy.util.AppLauncherUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import androidx.core.net.toUri

/**
 * ViewModel for the app-specific settings screen.
 */
@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    private val appLauncherUtils: AppLauncherUtils,
    @param:ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get the package name from the saved state handle
    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    // UI state
    private val _uiState = MutableStateFlow(AppSettingsUiState())
    val uiState: StateFlow<AppSettingsUiState> = _uiState.asStateFlow()

    // App info
    private val _appInfo = MutableStateFlow<AppInfo?>(null)
    val appInfo: StateFlow<AppInfo?> = _appInfo.asStateFlow()

    init {
        // Load app info
        viewModelScope.launch {
            appUsageRepository.getAppsWithUsageInfo().collect { apps ->
                val app = apps.find { it.packageName == packageName }
                _appInfo.value = app
                
                // Update UI state with app info
                app?.let {
                    _uiState.value = _uiState.value.copy(
                        appName = it.label,
                        lastUsedTime = formatLastUsedTime(it.lastUsedTimestamp),
                        showOnlyInSearch = false // TODO: Implement this feature
                    )
                }
            }
        }
    }

    /**
     * Formats the last used timestamp into a readable string.
     */
    private fun formatLastUsedTime(timestamp: Long): String {
        if (timestamp == 0L) {
            return "Never used"
        }
        
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    /**
     * Launches the app.
     */
    fun launchApp() {
        appLauncherUtils.launchApp(packageName)
    }

    /**
     * Removes the app from recently used.
     */
    fun removeFromRecentlyUsed() {
        viewModelScope.launch {
            appUsageRepository.removeUsageRecord(packageName)
            _uiState.value = _uiState.value.copy(
                statusMessage = "Removed from recently used"
            )
        }
    }

    /**
     * Sets whether to show the app only in search.
     */
    fun setShowOnlyInSearch(showOnlyInSearch: Boolean) {
        // TODO: Implement this feature
        _uiState.value = _uiState.value.copy(
            showOnlyInSearch = showOnlyInSearch,
            statusMessage = if (showOnlyInSearch) "App will only show in search" else "App will show in all views"
        )
    }

    /**
     * Uninstalls the app.
     */
    fun uninstallApp() {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = "package:$packageName".toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

/**
 * UI state for the app-specific settings screen.
 */
data class AppSettingsUiState(
    val appName: String = "",
    val lastUsedTime: String = "",
    val showOnlyInSearch: Boolean = false,
    val statusMessage: String = ""
)