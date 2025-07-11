package me.semoro.revy.ui.permissions

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.semoro.revy.data.repository.AppUsageRepository
import me.semoro.revy.util.PermissionUtils

/**
 * Screen for requesting usage stats permission.
 *
 * @param onPermissionGranted Callback when permission is granted
 * @param permissionUtils Utility for checking and requesting permissions
 * @param viewModel ViewModel for the permission screen
 */
@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit,
    permissionUtils: PermissionUtils,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    var hasPermission by remember { mutableStateOf(viewModel.hasUsageStatsPermission()) }
    
    // Check permission on launch and when returning from settings
    LaunchedEffect(key1 = hasPermission) {
        if (hasPermission) {
            onPermissionGranted()
        }
    }
    
    // Activity result launcher for the usage access settings
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Check permission again after returning from settings
        hasPermission = viewModel.hasUsageStatsPermission()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Usage Access Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Revy Launcher needs access to your app usage statistics to show your recently used apps.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                val intent = permissionUtils.createUsageAccessSettingsIntent()
                settingsLauncher.launch(intent)
            }
        ) {
            Text(text = "Grant Permission")
        }
    }
}