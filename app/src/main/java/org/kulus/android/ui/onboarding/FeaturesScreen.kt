package org.kulus.android.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.kulus.android.ui.theme.*

@Composable
fun FeaturesScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatrixBackground)
            .padding(32.dp)
    ) {
        Text(
            text = "What Kulus Does",
            style = MaterialTheme.typography.headlineMedium,
            color = MatrixNeon
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            FeatureItem(
                icon = Icons.Default.PhotoCamera,
                title = "Photo OCR",
                description = "Scan glucose meter screens automatically with your camera"
            )

            FeatureItem(
                icon = Icons.Default.ShowChart,
                title = "Trends & Analytics",
                description = "View charts, statistics, time-in-range, and A1C estimates"
            )

            FeatureItem(
                icon = Icons.Default.CloudSync,
                title = "Cloud Sync",
                description = "Your data automatically syncs across all your devices"
            )

            FeatureItem(
                icon = Icons.Default.Lock,
                title = "Privacy First",
                description = "Your data is private, encrypted, and segregated from other users"
            )

            FeatureItem(
                icon = Icons.Default.Description,
                title = "Export Data",
                description = "Export your readings to CSV, JSON, or detailed PDF reports"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        OnboardingNavigationButtons(
            onBack = onBack,
            onNext = onNext
        )

        Spacer(modifier = Modifier.height(16.dp))

        PageIndicator(
            currentPage = 1,
            totalPages = 5,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
