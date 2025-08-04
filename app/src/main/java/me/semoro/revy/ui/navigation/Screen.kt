package me.semoro.revy.ui.navigation

/**
 * Enum representing the different screens in the app.
 *
 * @property route The route used for navigation
 */
enum class Screen(val route: String) {
    /**
     * The permission screen shown when the app needs usage stats permission.
     */
    PERMISSION("permission"),

    /**
     * The home screen showing the app grid.
     */
    HOME("home"),

    /**
     * The widgets screen for managing widgets.
     */
    WIDGETS("widgets"),

    /**
     * The settings screen.
     */
    SETTINGS("settings"),

    /**
     * The app-specific settings screen.
     */
    APP_SETTINGS("app_settings/{packageName}")
}
