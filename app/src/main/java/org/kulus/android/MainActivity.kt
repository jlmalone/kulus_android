package org.kulus.android

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kulus.android.data.preferences.PreferencesRepository
import org.kulus.android.data.repository.KulusV3Repository
import org.kulus.android.service.BiometricService
import org.kulus.android.service.NetworkMonitor
import org.kulus.android.service.SyncQueueManager
import org.kulus.android.ui.screens.*
import org.kulus.android.ui.screens.onboarding.OnboardingNav
import org.kulus.android.ui.theme.KulusTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    lateinit var biometricService: BiometricService

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var syncQueueManager: SyncQueueManager

    @Inject
    lateinit var kulusRepository: KulusV3Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Auto-sync on startup when phone is configured
        triggerStartupSync()

        // Re-sync on connectivity restore
        observeNetworkForSync()

        setContent {
            KulusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KulusApp(preferencesRepository, biometricService)
                }
            }
        }
    }

    /**
     * Auto-sync on startup: if phone number is configured, process pending queue + fetch latest.
     * Matches iOS DataService auto-sync behavior.
     */
    private fun triggerStartupSync() {
        lifecycleScope.launch {
            try {
                val prefs = preferencesRepository.userPreferencesFlow.first()
                if (!prefs.phoneNumber.isNullOrBlank()) {
                    Log.d(TAG, "Startup sync: phone configured, processing queue...")
                    syncQueueManager.processPendingQueue()
                    kulusRepository.syncReadingsFromServer()
                    Log.d(TAG, "Startup sync complete")
                } else {
                    Log.d(TAG, "Startup sync skipped: no phone number configured")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Startup sync failed: ${e.message}")
            }
        }
    }

    /**
     * Observe network connectivity and trigger sync when connection is restored.
     * Matches iOS NWPathMonitor behavior.
     */
    private fun observeNetworkForSync() {
        lifecycleScope.launch {
            var wasDisconnected = false
            networkMonitor.connectivityFlow.collect { isConnected ->
                if (isConnected && wasDisconnected) {
                    Log.d(TAG, "Network restored, triggering sync...")
                    try {
                        syncQueueManager.processPendingQueue()
                        kulusRepository.syncReadingsFromServer()
                    } catch (e: Exception) {
                        Log.w(TAG, "Network-restore sync failed: ${e.message}")
                    }
                }
                wasDisconnected = !isConnected
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun KulusApp(
    preferencesRepository: PreferencesRepository,
    biometricService: BiometricService
) {
    val userPreferences by preferencesRepository.userPreferencesFlow.collectAsState(
        initial = org.kulus.android.data.preferences.UserPreferences()
    )
    var showOnboarding by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(false) }

    // Determine initial state based on onboarding completion
    LaunchedEffect(userPreferences.onboardingCompleted) {
        showOnboarding = !userPreferences.onboardingCompleted
    }

    // If biometric is not enabled, auto-authenticate
    LaunchedEffect(userPreferences.biometricEnabled) {
        if (!userPreferences.biometricEnabled) {
            isAuthenticated = true
        }
    }

    if (showOnboarding) {
        OnboardingNav(
            onOnboardingComplete = {
                showOnboarding = false
            }
        )
    } else if (userPreferences.biometricEnabled && !isAuthenticated) {
        BiometricPromptScreen(
            biometricService = biometricService,
            onAuthenticated = {
                isAuthenticated = true
            }
        )
    } else {
        MainApp()
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(
                onAddClick = { navController.navigate("add_reading") },
                onReadingClick = { id ->
                    navController.navigate("reading_detail/$id")
                },
                onSignedOut = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onHelpClick = { navController.navigate("help") },
                onApiLogClick = { navController.navigate("api_log") }
            )
        }

        composable("add_reading") { backStackEntry ->
            val scannedValue = backStackEntry.savedStateHandle.get<Double>("scannedValue")
            val scannedUnit = backStackEntry.savedStateHandle.get<String>("scannedUnit")
            val scannedPhotoUri = backStackEntry.savedStateHandle.get<String>("scannedPhotoUri")

            AddReadingScreen(
                onSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() },
                onScanClick = { navController.navigate("camera") },
                scannedValue = scannedValue,
                scannedUnit = scannedUnit,
                scannedPhotoUri = scannedPhotoUri
            )
        }

        composable("camera") {
            CameraScreen(
                onValueExtracted = { value, unit, photoUri ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scannedValue", value)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scannedUnit", unit)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scannedPhotoUri", photoUri)
                    navController.popBackStack()
                },
                onManualEntry = {
                    navController.popBackStack()
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "reading_detail/{readingId}",
            arguments = listOf(navArgument("readingId") { type = NavType.StringType })
        ) {
            ReadingDetailScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { id -> }
            )
        }

        composable("help") {
            HelpScreen(onBackClick = { navController.popBackStack() })
        }

        composable("api_log") {
            ApiLogScreen(onBackClick = { navController.popBackStack() })
        }
    }
}
