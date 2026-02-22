package org.kulus.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Light Color Scheme (iOS-style)
private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlue.copy(alpha = 0.1f),
    onPrimaryContainer = BrandBlue,
    secondary = BrandGreen,
    onSecondary = Color.White,
    secondaryContainer = BrandGreen.copy(alpha = 0.1f),
    onSecondaryContainer = BrandGreen,
    tertiary = BrandMint,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFC6C6C8),  // iOS separator color
    outlineVariant = Color(0xFFE5E5EA),
    error = Color(0xFFFF3B30),  // iOS red
    onError = Color.White,
    errorContainer = Color(0xFFFF3B30).copy(alpha = 0.1f),
    onErrorContainer = Color(0xFFFF3B30)
)

// Dark Color Scheme (iOS-style)
private val DarkColorScheme = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlue.copy(alpha = 0.2f),
    onPrimaryContainer = BrandBlue,
    secondary = BrandGreen,
    onSecondary = Color.White,
    secondaryContainer = BrandGreen.copy(alpha = 0.2f),
    onSecondaryContainer = BrandGreen,
    tertiary = BrandMint,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF48484A),  // iOS separator color dark
    outlineVariant = Color(0xFF38383A),
    error = Color(0xFFFF453A),  // iOS red dark
    onError = Color.White,
    errorContainer = Color(0xFFFF453A).copy(alpha = 0.2f),
    onErrorContainer = Color(0xFFFF453A)
)

@Composable
fun KulusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
