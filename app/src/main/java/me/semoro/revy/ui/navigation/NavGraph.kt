package me.semoro.revy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.semoro.revy.ui.home.HomeScreen
import me.semoro.revy.ui.permissions.PermissionScreen
import me.semoro.revy.ui.permissions.PermissionViewModel
import me.semoro.revy.ui.settings.SettingsScreen
import me.semoro.revy.util.PermissionUtils

/**
 * Navigation graph for the app.
 *
 * @param navController The navigation controller
 * @param startDestination The start destination
 * @param permissionUtils Utility for checking and requesting permissions
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.HOME.route,
    permissionUtils: PermissionUtils
) {
    val actions = remember(navController) { NavActions(navController) }

    // Check if we have usage stats permission
    val permissionViewModel: PermissionViewModel = hiltViewModel()

    // If we don't have permission, start at the permission screen
    val actualStartDestination = if (permissionViewModel.hasAllRequiredPermissions()) {
        startDestination
    } else {
        Screen.PERMISSION.route
    }

    NavHost(
        navController = navController,
        startDestination = actualStartDestination
    ) {
        // Permission screen
        composable(Screen.PERMISSION.route) {
            PermissionScreen(
                onPermissionGranted = actions.navigateToHome,
                permissionUtils = permissionUtils
            )
        }

        // Home screen
        composable(Screen.HOME.route) {
            HomeScreen(
                onNavigateToSettings = actions.navigateToSettings
            )
        }

        // Settings screen
        composable(Screen.SETTINGS.route) {
            SettingsScreen()
        }

        // Other screens will be added later
    }
}

/**
 * Navigation actions for the app.
 */
class NavActions(private val navController: NavHostController) {

    /**
     * Navigates to the home screen.
     */
    val navigateToHome: () -> Unit = {
        navController.navigate(Screen.HOME.route) {
            // Pop up to the start destination of the graph to avoid building up a large stack
            // of destinations on the back stack as users select items
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }

    /**
     * Navigates to the settings screen.
     */
    val navigateToSettings: () -> Unit = {
        navController.navigate(Screen.SETTINGS.route) {
            // Avoid multiple copies of the same destination when reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}
