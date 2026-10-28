package com.example.quickpair

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quickpair.navigation.Screen
import com.example.quickpair.ui.screens.AboutScreen
import com.example.quickpair.ui.screens.GameScreen
import com.example.quickpair.ui.screens.HomeScreen
import com.example.quickpair.ui.screens.ResultScreen
import com.example.quickpair.ui.screens.SettingsScreen
import com.example.quickpair.ui.screens.SplashScreen
import com.example.quickpair.ui.theme.QuickPairTheme
import com.example.quickpair.ui.viewmodel.GameNavigationEvent
import com.example.quickpair.ui.viewmodel.QuickPairViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: QuickPairViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            QuickPairTheme(themeMode = themeMode) {
                QuickPairApp(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun QuickPairApp(
    viewModel: QuickPairViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val stats by viewModel.stats.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val reducedMotion by viewModel.reducedMotion.collectAsState()
    val gameState by viewModel.uiState.collectAsState()
    val lastResult by viewModel.lastResult.collectAsState()

    // Listen to ViewModel navigation events
    LaunchedEffect(viewModel) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is GameNavigationEvent.NavigateToResult -> {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
                is GameNavigationEvent.NavigateToHome -> {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                stats = stats,
                onPlayClicked = { difficulty ->
                    viewModel.startGame(difficulty)
                    navController.navigate(Screen.Game.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                state = gameState,
                reducedMotion = reducedMotion,
                onTileClicked = { tile -> viewModel.onTileClicked(tile) },
                onPauseClicked = { viewModel.pauseGame() },
                onResumeClicked = { viewModel.resumeGame() },
                onRequestRestart = { viewModel.requestRestart() },
                onConfirmRestart = { viewModel.confirmRestart() },
                onDismissRestart = { viewModel.dismissRestartConfirm() },
                onRequestExit = { viewModel.requestExit() },
                onConfirmExit = { viewModel.confirmExit() },
                onDismissExit = { viewModel.dismissExitConfirm() }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                result = lastResult,
                stats = stats,
                onPlayAgain = {
                    viewModel.playAgain()
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                currentTheme = themeMode,
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                reducedMotion = reducedMotion,
                onThemeChanged = { viewModel.setThemeMode(it) },
                onSoundChanged = { viewModel.setSoundEnabled(it) },
                onHapticsChanged = { viewModel.setHapticsEnabled(it) },
                onReducedMotionChanged = { viewModel.setReducedMotion(it) },
                onResetStats = { viewModel.resetStatistics() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
