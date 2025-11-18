package org.kulus.android.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.kulus.android.ui.theme.*

@Composable
fun AccountSetupScreen(
    onComplete: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var confirmName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatrixBackground)
            .padding(32.dp)
    ) {
        Text(
            text = "Set Up Your Account",
            style = MaterialTheme.typography.headlineMedium,
            color = MatrixNeon
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Enter your name or phone number",
                style = MaterialTheme.typography.bodyLarge,
                color = MatrixTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "This will be used to identify your data. Use the same identifier on all your devices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MatrixTextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    showError = false
                },
                label = { Text("Name or Phone Number") },
                placeholder = { Text("e.g., John Smith or (555) 123-4567") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MatrixNeon,
                    focusedLabelColor = MatrixNeon,
                    cursorColor = MatrixNeon
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmName,
                onValueChange = {
                    confirmName = it
                    showError = false
                },
                label = { Text("Confirm Name or Phone Number") },
                placeholder = { Text("Type it again to confirm") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = showError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MatrixNeon,
                    focusedLabelColor = MatrixNeon,
                    cursorColor = MatrixNeon,
                    errorBorderColor = GlucoseRed,
                    errorLabelColor = GlucoseRed
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            if (showError) {
                Text(
                    text = errorMessage,
                    color = GlucoseRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MatrixAmber.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MatrixAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Examples:\n• Full name: \"Sarah Johnson\"\n• Phone: \"(510) 555-1234\"\n• Email: \"sarah@example.com\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MatrixTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OnboardingNavigationButtons(
            onBack = onBack,
            onNext = {
                when {
                    name.isBlank() -> {
                        showError = true
                        errorMessage = "Please enter your name or phone number"
                    }
                    name.length < 3 -> {
                        showError = true
                        errorMessage = "Name must be at least 3 characters"
                    }
                    confirmName.isBlank() -> {
                        showError = true
                        errorMessage = "Please confirm your name"
                    }
                    name != confirmName -> {
                        showError = true
                        errorMessage = "Names don't match. Please try again."
                    }
                    else -> {
                        onComplete(name.trim())
                    }
                }
            },
            nextEnabled = name.isNotBlank() && confirmName.isNotBlank(),
            nextText = "Complete Setup"
        )

        Spacer(modifier = Modifier.height(16.dp))

        PageIndicator(
            currentPage = 3,
            totalPages = 5,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
