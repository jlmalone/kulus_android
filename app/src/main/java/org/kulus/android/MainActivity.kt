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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.kulus.android.ui.screens.AddReadingScreen
import org.kulus.android.ui.screens.ReadingsListScreen
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
                    // TODO: Implement reading detail screen
                }
            )
        }

        composable("add_reading") {
            AddReadingScreen(
                onSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
