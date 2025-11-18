package org.kulus.android.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.kulus.android.ui.theme.*

/**
 * Shared components for onboarding screens
 */

@Composable
fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MatrixNeon,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MatrixTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MatrixTextSecondary
            )
        }
    }
}

@Composable
fun PrivacyPoint(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MatrixAmber
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MatrixTextSecondary
        )
    }
}

@Composable
fun OnboardingNavigationButtons(
    onBack: (() -> Unit)?,
    onNext: () -> Unit,
    nextEnabled: Boolean = true,
    nextText: String = "Continue",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (onBack != null) {
            Arrangement.SpaceBetween
        } else {
            Arrangement.End
        }
    ) {
        if (onBack != null) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MatrixTextPrimary
                )
            ) {
                Text("Back")
            }
        }
        Button(
            onClick = onNext,
            enabled = nextEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MatrixNeon,
                contentColor = MatrixBackground
            )
        ) {
            Text(nextText)
        }
    }
}

@Composable
fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (index == currentPage) 12.dp else 8.dp)
                    .then(
                        if (index == currentPage) {
                            Modifier
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = if (index == currentPage) MatrixNeon else MatrixTextSecondary.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Box(modifier = Modifier.size(if (index == currentPage) 12.dp else 8.dp))
                }
            }
        }
    }
}
