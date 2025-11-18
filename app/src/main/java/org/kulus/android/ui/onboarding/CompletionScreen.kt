package org.kulus.android.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.kulus.android.ui.theme.*

@Composable
fun CompletionScreen(
    userName: String,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(2500)
        onFinish()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatrixBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GlucoseGreen,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "All Set!",
            style = MaterialTheme.typography.headlineLarge,
            color = MatrixNeon
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome, $userName",
            style = MaterialTheme.typography.titleLarge,
            color = MatrixTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your account has been created and you're ready to track your glucose readings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MatrixTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        CircularProgressIndicator(
            color = MatrixNeon,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        PageIndicator(
            currentPage = 4,
            totalPages = 5
        )
    }
}
