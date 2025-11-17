package org.kulus.android.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.kulus.android.R
import org.kulus.android.ui.theme.*

@Composable
fun WelcomeScreen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatrixBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hamumu butterfly logo
        Image(
            painter = painterResource(R.mipmap.ic_launcher),
            contentDescription = "Kulus Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Welcome to Kulus",
            style = MaterialTheme.typography.headlineLarge,
            color = MatrixNeon,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Track your glucose readings securely\nwith privacy and precision",
            style = MaterialTheme.typography.bodyLarge,
            color = MatrixTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MatrixNeon,
                contentColor = MatrixBackground
            )
        ) {
            Text(
                "Get Started",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PageIndicator(
            currentPage = 0,
            totalPages = 5
        )
    }
}
