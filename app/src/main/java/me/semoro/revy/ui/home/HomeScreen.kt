package me.semoro.revy.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
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
                    onAppLongClick = { viewModel.togglePinApp(it.packageName, !it.isPinned) },
                    uiState = uiState,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearchActiveChange = { viewModel.setSearchActive(it) }
                )
            }
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
                    modifier = Modifier,
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
 * @param uiState Current UI state
 * @param onSearchQueryChange Callback when search query changes
 * @param onSearchActiveChange Callback when search active state changes
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridByBucket(
    appsByBucket: Map<RecencyBucket, List<AppInfo>>,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    uiState: HomeUiState = HomeUiState(),
    onSearchQueryChange: (String) -> Unit = {},
    onSearchActiveChange: (Boolean) -> Unit = {}
) {
    // Create a list of pages, where each page contains a bucket and a subset of apps
    val pages = remember(appsByBucket, uiState.searchQuery, uiState.isSearchActive, uiState.searchResults) {
        val pages = mutableListOf<Pair<RecencyBucket, List<AppInfo>>>()

        // The number of apps per page (4 columns x 7 rows)
        val appsPerPage = 28

        // First, add all regular recency bucket pages
        val regularBuckets = listOf(
            RecencyBucket.TODAY,
            RecencyBucket.THIS_WEEK,
            RecencyBucket.LAST_30_DAYS
        )

        regularBuckets.forEach { bucket ->
            val apps = appsByBucket[bucket] ?: emptyList()
            if (apps.isNotEmpty()) {
                // Split apps into chunks of appsPerPage
                val chunkedApps = apps.chunked(appsPerPage)

                // Add each chunk as a separate page with the same bucket
                chunkedApps.forEach { chunk ->
                    pages.add(Pair(bucket, chunk))
                }
            }
        }

        // Add search page after regular pages
        if (uiState.isSearchActive && uiState.searchResults.isNotEmpty()) {
            // If search is active and we have results, add them to pages
            val searchResults = uiState.searchResults.chunked(appsPerPage)
            searchResults.forEach { chunk ->
                pages.add(Pair(RecencyBucket.SEARCH, chunk))
            }
        } else {
            // Add an empty search page
            pages.add(Pair(RecencyBucket.SEARCH, emptyList()))
        }

        pages
    }


    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal pager for swiping between pages

        // Create pager state with the total number of pages
        val pagerState = rememberPagerState(pageCount = { pages.size })
        val (bucket, _) = pages[pagerState.targetPage]

        // Calculate the current page index and total pages for this bucket
        BucketHeader(
            bucket = bucket,
            currentPage = pagerState.targetPage,
            pageCount = pages.size,
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onSearchActiveChange = onSearchActiveChange
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) { pageIndex ->
            val (_, pageApps) = pages[pageIndex]

            // Bucket header
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                repeat(7) { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(4) { col ->

                            val app = pageApps.getOrNull(row * 4 + col)
                            if (app != null) {
                                AppIcon(
                                    modifier = Modifier.weight(1f),
                                    app = app,
                                    onClick = { onAppClick(app) },
                                    onLongClick = { onAppLongClick(app) }
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Header for a recency bucket.
 *
 * @param bucket The recency bucket
 * @param currentPage The current page index
 * @param pageCount The total number of pages
 * @param searchQuery The current search query
 * @param onSearchQueryChange Callback when search query changes
 * @param onSearchActiveChange Callback when search active state changes
 */
@Composable
fun BucketHeader(
    bucket: RecencyBucket,
    currentPage: Int = 0,
    pageCount: Int = 1,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearchActiveChange: (Boolean) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (bucket == RecencyBucket.SEARCH) {
                val focusRequester = remember { FocusRequester() }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { onSearchQueryChange(it) },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(0.8f),
                    placeholder = { Text("Search apps...") },
                    singleLine = true
                )

                LaunchedEffect(bucket) {
                    focusRequester.requestFocus()
                    onSearchActiveChange(true)
                }
            } else {
                // If we're navigating away from search, reset search
                LaunchedEffect(bucket) {
                    if (searchQuery.isNotEmpty()) {
                        onSearchActiveChange(false)
                    }
                }

                Text(
                    text = bucket.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (pageCount > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pageCount) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentPage) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Icon for an app.
 *
 * @param app The app info
 * @param onClick Callback when the icon is clicked
 * @param onLongClick Callback when the icon is long-clicked
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIcon(
    modifier: Modifier,
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(72.dp)
            .height(86.dp)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            )
    ) {

        val bitmap = remember(app) { app.icon.asImageBitmap() }
        androidx.compose.foundation.Image(
            bitmap = bitmap,
            contentDescription = app.label,
            modifier = Modifier
                .size(56.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = app.label,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
