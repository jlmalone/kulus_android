package org.kulus.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KulusColorScheme = darkColorScheme(
    primary = MatrixNeon,
    onPrimary = MatrixBackground,
    primaryContainer = MatrixPanelEnd,  // Dark green panel, NO PURPLE
    onPrimaryContainer = MatrixNeon,
    secondary = MatrixAmber,
    onSecondary = MatrixBackground,
    secondaryContainer = MatrixPanelEnd,  // Consistent with primary
    onSecondaryContainer = MatrixAmber,
    tertiary = MatrixNeonDim,
    onTertiary = MatrixBackground,
    tertiaryContainer = MatrixPanelEnd,  // Consistent
    onTertiaryContainer = MatrixNeonDim,
    background = MatrixBackground,
    onBackground = MatrixTextPrimary,
    surface = MatrixPanelStart,
    onSurface = MatrixTextPrimary,
    surfaceVariant = MatrixPanelEnd,
    onSurfaceVariant = MatrixTextSecondary,
    outline = MatrixNeonDim,
    outlineVariant = MatrixNeonDim.copy(alpha = 0.4f),
    scrim = Color.Black.copy(alpha = 0.8f),
    inverseOnSurface = MatrixBackground,
    inverseSurface = MatrixTextPrimary,
    inversePrimary = MatrixNeon.copy(alpha = 0.7f),  // No default purple
    error = GlucoseRed,
    onError = Color.White,
    errorContainer = GlucoseRed.copy(alpha = 0.2f),
    onErrorContainer = GlucoseRed
)

@Composable
fun KulusTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KulusColorScheme,
        typography = Typography,
        content = content
    )
}
