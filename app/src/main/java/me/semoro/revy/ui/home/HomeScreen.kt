package me.semoro.revy.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.model.RecencyBucket
import me.semoro.revy.util.AppLauncherUtils

/**
 * Home screen showing the app grid.
 *
 * @param viewModel ViewModel for the home screen
 * @param appLauncherUtils Utility for launching apps
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val appLauncherUtils = remember { AppLauncherUtils(context) }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Pinned apps strip
        if (uiState.pinnedApps.isNotEmpty()) {
            PinnedAppsStrip(
                pinnedApps = uiState.pinnedApps,
                onAppClick = { appLauncherUtils.launchApp(it.packageName) },
                onAppLongClick = { viewModel.togglePinApp(it.packageName, false) }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // App grid by recency bucket
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AppGridByBucket(
                appsByBucket = uiState.appsByBucket,
                onAppClick = { appLauncherUtils.launchApp(it.packageName) },
                onAppLongClick = { viewModel.togglePinApp(it.packageName, !it.isPinned) }
            )
        }
    }
}

/**
 * Strip of pinned apps at the top of the screen.
 *
 * @param pinnedApps List of pinned apps
 * @param onAppClick Callback when an app is clicked
 * @param onAppLongClick Callback when an app is long-clicked
 */
@Composable
fun PinnedAppsStrip(
    pinnedApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Pinned",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(pinnedApps) { app ->
                AppIcon(
                    app = app,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) }
                )
            }
        }
    }
}

/**
 * Grid of apps grouped by recency bucket.
 *
 * @param appsByBucket Map of recency bucket to list of apps
 * @param onAppClick Callback when an app is clicked
 * @param onAppLongClick Callback when an app is long-clicked
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridByBucket(
    appsByBucket: Map<RecencyBucket, List<AppInfo>>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Sort buckets by their order in the enum
        val sortedBuckets = RecencyBucket.values().filter { appsByBucket.containsKey(it) }

        for (bucket in sortedBuckets) {
            val apps = appsByBucket[bucket] ?: continue
            if (apps.isEmpty()) continue

            // Bucket header
            stickyHeader {
                BucketHeader(bucket = bucket)
            }

            // Apps in this bucket
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp * ((apps.size / 4) + if (apps.size % 4 > 0) 1 else 0))
                ) {
                    items(apps) { app ->
                        AppIcon(
                            app = app,
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongClick(app) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Header for a recency bucket.
 *
 * @param bucket The recency bucket
 */
@Composable
fun BucketHeader(bucket: RecencyBucket) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = bucket.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Icon for an app.
 *
 * @param app The app info
 * @param onClick Callback when the icon is clicked
 * @param onLongClick Callback when the icon is long-clicked
 */
@Composable
fun AppIcon(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    bitmap = app.icon.toBitmap().asImageBitmap(),
                    contentDescription = app.label,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = app.label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
