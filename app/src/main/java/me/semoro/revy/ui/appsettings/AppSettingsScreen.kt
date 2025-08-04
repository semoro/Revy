package me.semoro.revy.ui.appsettings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * App-specific settings screen where users can perform actions on a specific app.
 *
 * @param viewModel The ViewModel for this screen
 * @param onNavigateBack Callback to navigate back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    viewModel: AppSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val appInfo by viewModel.appInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "App Settings: ${uiState.appName}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // Use a back arrow icon
                        Text("<")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App icon and name
            appInfo?.let { app ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // App icon
                    androidx.compose.foundation.Image(
                        bitmap = app.icon.asImageBitmap(),
                        contentDescription = app.label,
                        modifier = Modifier.size(56.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // App name and last used time
                    Column {
                        Text(
                            text = app.label,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = "Last used: ${uiState.lastUsedTime}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            HorizontalDivider()

            // Launch app button
            Button(
                onClick = { viewModel.launchApp() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Launch App")
            }

            // Remove from recently used button
            Button(
                onClick = { viewModel.removeFromRecentlyUsed() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remove from Recently Used")
            }

            // Show only in search toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Show App Only in Search",
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = uiState.showOnlyInSearch,
                    onCheckedChange = { viewModel.setShowOnlyInSearch(it) }
                )
            }

            // Uninstall app button
            Button(
                onClick = { viewModel.uninstallApp() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Uninstall App")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Status message
            if (uiState.statusMessage.isNotEmpty()) {
                Text(
                    text = uiState.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
