package org.kulus.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import org.kulus.android.ui.screens.AddReadingScreen
import org.kulus.android.ui.screens.ReadingDetailScreen
import org.kulus.android.ui.screens.ReadingsListScreen
import org.kulus.android.ui.screens.SettingsScreen
import org.kulus.android.ui.theme.KulusTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KulusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KulusApp()
                }
            }
        }
    }
}

@Composable
fun KulusApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "readings_list"
    ) {
        composable("readings_list") {
            ReadingsListScreen(
                onAddClick = { navController.navigate("add_reading") },
                onReadingClick = { id ->
                    navController.navigate("reading_detail/$id")
                },
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        composable("add_reading") {
            AddReadingScreen(
                onSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "reading_detail/{readingId}",
            arguments = listOf(navArgument("readingId") { type = NavType.StringType })
        ) {
            ReadingDetailScreen(
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    // TODO: Implement edit functionality
                    // navController.navigate("edit_reading/$id")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onSignedOut = {
                    // Navigate back to readings list after sign out
                    navController.navigate("readings_list") {
                        popUpTo("readings_list") { inclusive = true }
                    }
                }
            )
        }
    }
}
