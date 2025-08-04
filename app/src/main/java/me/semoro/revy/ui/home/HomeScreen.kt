package me.semoro.revy.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.semoro.revy.data.model.AppInfo
import me.semoro.revy.data.model.SlotInfo

/**
 * Home screen showing the app grid.
 *
 * @param viewModel ViewModel for the home screen
 * @param onNavigateToSettings Callback to navigate to the settings screen
 * @param onNavigateToAppSettings Callback to navigate to the app-specific settings screen
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToAppSettings: (String) -> Unit = {}
) {
    val appLauncherUtils = viewModel.appLauncherUtils

    val isLoading by viewModel.isLoading.collectAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // App grid by recency bucket
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AppGridByBucket(
                    viewModel,
                    onAppClick = { appLauncherUtils.launchApp(it.packageName) },
                    onAppLongClick = { onNavigateToAppSettings(it.packageName) },
                    onHeaderLongClick = {
                        onNavigateToSettings()
                    }
                )
            }
        }
    }
}

@Composable
operator fun <T> StateFlow<T>.provideDelegate(a: Any?, b: Any?): State<T> {
    return this.collectAsState()
}

/**
 * Grid of apps grouped by recency bucket.
 *
 * @param onAppClick Callback when an app is clicked
 * @param onAppLongClick Callback when an app is long-clicked
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridByBucket(
    viewModel: HomeViewModel = hiltViewModel(),
    onHeaderLongClick: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit
) {
    val pages by viewModel.pages
    val searchState by viewModel.searchState

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal pager for swiping between pages

        // Create pager state with the total number of pages
        val pagerState = rememberPagerState(pageCount = { pages.size })

        // Get the current page
        val currentPage = pages.getOrNull(pagerState.targetPage)

        // Show header based on page type
        if (currentPage != null) {
            val coroutineScope = rememberCoroutineScope()
            BucketHeader(
                page = currentPage,
                currentPage = pagerState.targetPage,
                pageCount = pages.size,
                searchQuery = searchState.query,
                pages = pages,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onSearchActiveChange = { viewModel.setSearchActive(it) },
                onResetSearch = {
                    viewModel.setSearchActive(false)
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pages.indexOfLast { it !is Page.SearchPage })
                    }
                },
                onHeaderLongClick = onHeaderLongClick
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp, bottom = 8.dp)
        ) { pageIndex ->
            val page = pages.getOrNull(pageIndex)

            if (page != null) {
                // Get apps based on page type
                val pageApps = when (page) {
                    is Page.BucketPage -> page.apps
                    is Page.SearchPage -> page.apps
                }

                // Display apps in a grid
                val rowCount = if (page is Page.SearchPage) 3 else 7

                // Get IME padding
                val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
                val imePadding = if (page is Page.SearchPage && imeVisible) {
                    with(LocalDensity.current) {
                        WindowInsets.ime.getBottom(this).toDp()
                    }
                } else {
                    0.dp
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp), 
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = imePadding)
                ) {
                    repeat(rowCount) { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(4) { col ->
                                val app = pageApps.getOrNull(row * 4 + col)
                                when (app) {
                                    is AppInfo -> {
                                       AppIcon(
                                           modifier = Modifier.weight(1f),
                                           app = app,
                                           onClick = { onAppClick(app) },
                                           onLongClick = { onAppLongClick(app) }
                                       )
                                    }
                                    null, SlotInfo.Gravestone -> Spacer(Modifier.weight(1f))
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Header for a page.
 *
 * @param currentPage The current page index
 * @param pageCount The total number of pages
 * @param searchQuery The current search query
 * @param onSearchQueryChange Callback when search query changes
 * @param onSearchActiveChange Callback when search active state changes
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BucketHeader(
    page: Page,
    currentPage: Int = 0,
    pageCount: Int = 1,
    pages: List<Page>,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearchActiveChange: (Boolean) -> Unit = {},
    onResetSearch: () -> Unit = {},
    onHeaderLongClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(onLongClick = onHeaderLongClick, onClick = {})
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (page) {
                is Page.SearchPage -> {
                    val focusRequester = remember { FocusRequester() }

                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { onSearchQueryChange(it) },
                        modifier = Modifier
                            .focusRequester(focusRequester),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Previous),
                        keyboardActions = KeyboardActions(onAny = {
                            onResetSearch()
                        })
                    )

                    val isVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
                    var wasVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(key1 = isVisible, key2 = wasVisible) {
                        if (!isVisible) {
                            if (wasVisible) {
                                onResetSearch()
                            }
                        } else {
                            wasVisible = true
                        }
                    }

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                        onSearchActiveChange(true)
                    }
                }

                is Page.BucketPage -> {
                    // If we're navigating away from search, reset search
                    LaunchedEffect(Unit) {
                        if (searchQuery.isNotEmpty()) {
                            onSearchActiveChange(false)
                        }
                    }

                    Text(
                        text = page.bucket.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (pageCount > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (p in pages) {
                        val color = if (p == page)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        // Check if the current page is a search page
                        if (p is Page.SearchPage) {
                            // Use magnifying glass icon for search pages
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(12.dp),
                                tint = color
                            )
                        } else {
                            // Use dot for regular pages
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        }
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
    // State to track if the icon is being clicked
    var isClicked by remember { mutableStateOf(false) }

    // Animate the scale based on click state
    val scale by animateFloatAsState(
        targetValue = if (isClicked) 0.8f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        finishedListener = {
            if (isClicked) {
                // Launch the app after animation completes
                onClick()
                // Reset the click state
                isClicked = false
            }
        }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(72.dp)
            .height(86.dp)
            .combinedClickable(
                onClick = { 
                    // Start the animation when clicked
                    isClicked = true
                },
                onLongClick = { onLongClick() }
            )
    ) {
        val bitmap = remember(app) { app.icon.asImageBitmap() }
        androidx.compose.foundation.Image(
            bitmap = bitmap,
            contentDescription = app.label,
            modifier = Modifier
                .size(56.dp)
                .scale(scale) // Apply scale animation
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
