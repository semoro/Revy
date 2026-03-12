package me.semoro.revy

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.semoro.revy.data.repository.AppFrequencyRepository
import me.semoro.revy.data.repository.AppUsageRepository
import me.semoro.revy.ui.navigation.NavGraph
import me.semoro.revy.ui.theme.RevyTheme
import me.semoro.revy.util.PermissionUtils
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionUtils: PermissionUtils

    @Inject
    lateinit var appUsageRepository: AppUsageRepository

    @Inject
    lateinit var appFrequencyRepository: AppFrequencyRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)


        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        window.isNavigationBarContrastEnforced = false
        setContent {
            RevyTheme {
                NavGraph(permissionUtils = permissionUtils)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        this.lifecycleScope.launch {
            appUsageRepository.checkAppUsageActivity()
            appFrequencyRepository.processStaleScores()
        }
    }
}
