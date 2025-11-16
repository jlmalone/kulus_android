package org.kulus.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KulusColorScheme = darkColorScheme(
    primary = MatrixNeon,
    onPrimary = MatrixBackground,
    secondary = MatrixAmber,
    onSecondary = MatrixBackground,
    tertiary = MatrixNeonDim,
    onTertiary = MatrixBackground,
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
    error = GlucoseRed,
    onError = Color.White
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
