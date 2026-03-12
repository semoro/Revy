package me.semoro.revy.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.semoro.revy.data.preferences.LayoutMode

/**
 * Settings/Technical screen where users can perform technical actions.
 *
 * @param viewModel The ViewModel for this screen
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Technical Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        // Layout mode selector
        Text(
            text = "Layout Mode",
            style = MaterialTheme.typography.bodyLarge
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            LayoutMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = uiState.layoutMode == mode,
                    onClick = { viewModel.setLayoutMode(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index, LayoutMode.entries.size)
                ) {
                    Text(mode.label)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rescan usage events button
        Button(
            onClick = { viewModel.rescanUsageEvents() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Rescan Usage Events")
        }
        
        // Pack apps button
        Button(
            onClick = { viewModel.packApps() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Pack Apps (Clean Up Gravestones)")
        }
        
        // Reload apps
        Button(
            onClick = { viewModel.reloadApps() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            Text("Reload Apps")
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
        
        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}