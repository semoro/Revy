package me.semoro.revy.ui.appsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

/**
 * A custom slider button that requires sliding to the right to trigger an action.
 *
 * @param onUninstall Callback when the slider is fully dragged to the right
 * @param modifier Modifier for the slider
 */
@Composable
fun SlideToUninstallButton(
    onUninstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val height = 56.dp
    var width by remember { mutableStateOf(280f) }
    val density = LocalDensity.current

    // Track the position of the slider
    var offsetX by remember { mutableStateOf(0f) }
    var sliderReleased by remember { mutableStateOf(true) }

    // Calculate the threshold for triggering the action (e.g., 90% of the width)
    val threshold = width * 0.9f

    // Check if the slider has been dragged past the threshold
    LaunchedEffect(offsetX, sliderReleased) {
        if (offsetX >= threshold && sliderReleased) {
            // Trigger the uninstall action
            onUninstall()
            // Reset the slider
        } else if (sliderReleased && offsetX > 0f) {
            // Reset the slider when released before reaching the threshold
            offsetX = 0f
        }
    }

    // Create a draggable state
    val draggableState = rememberDraggableState { delta ->
        // Update the offset, but constrain it to the width of the slider
        val newOffset = (offsetX + delta).coerceIn(0f, width)
        offsetX = newOffset
    }

    Box(
        modifier = modifier
            .height(height)
            .fillMaxWidth()
            .onSizeChanged {
                with(density) {
                    width = it.width.toFloat() - height.toPx()
                }
            }
            .clip(RoundedCornerShape(height / 2))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Background text
        Text(
            text = "Uninstall App",
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Draggable knob
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .size(height - 8.dp)
                .clip(RoundedCornerShape(height / 2))
                .background(MaterialTheme.colorScheme.error)
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStarted = { sliderReleased = false },
                    onDragStopped = { sliderReleased = true }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ">",
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}

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

            SlideToUninstallButton(
                onUninstall = { viewModel.uninstallApp() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))
            val timestamps by viewModel.usageTimestamps.collectAsState(emptyList())

            Text("Usages (Last 90 days)")
            AppUsageFrequencyDisplay(timestamps)

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
