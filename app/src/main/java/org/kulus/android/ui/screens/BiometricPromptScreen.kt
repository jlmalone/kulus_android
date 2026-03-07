package org.kulus.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import org.kulus.android.service.BiometricService

/**
 * Lock screen shown when app opens and biometric authentication is enabled.
 * Displays the Kulus branding and auto-triggers the biometric prompt.
 * Falls back to device PIN/pattern via a manual button.
 */
@Composable
fun BiometricPromptScreen(
    biometricService: BiometricService,
    onAuthenticated: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasTriggered by remember { mutableStateOf(false) }

    // Auto-trigger biometric prompt on first composition
    LaunchedEffect(Unit) {
        if (!hasTriggered && activity != null) {
            hasTriggered = true
            biometricService.authenticate(activity) { result ->
                when (result) {
                    is BiometricService.AuthResult.Success -> {
                        onAuthenticated()
                    }
                    is BiometricService.AuthResult.Error -> {
                        errorMessage = result.message
                    }
                    is BiometricService.AuthResult.Failed -> {
                        errorMessage = "Authentication failed. Try again."
                    }
                    is BiometricService.AuthResult.Cancelled -> {
                        errorMessage = "Authentication cancelled."
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lock icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App name
            Text(
                text = "Kulus",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Authenticate to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Retry / Use PIN button
            OutlinedButton(
                onClick = {
                    errorMessage = null
                    if (activity != null) {
                        biometricService.authenticate(activity) { result ->
                            when (result) {
                                is BiometricService.AuthResult.Success -> {
                                    onAuthenticated()
                                }
                                is BiometricService.AuthResult.Error -> {
                                    errorMessage = result.message
                                }
                                is BiometricService.AuthResult.Failed -> {
                                    errorMessage = "Authentication failed. Try again."
                                }
                                is BiometricService.AuthResult.Cancelled -> {
                                    errorMessage = "Authentication cancelled."
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlock")
            }
        }
    }
}
