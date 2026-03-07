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

// Light Color Scheme (Earth & Wellness)
private val LightColorScheme = lightColorScheme(
    primary = EarthSageGreen,
    onPrimary = Color.White,
    primaryContainer = EarthSageGreen.copy(alpha = 0.12f),
    onPrimaryContainer = Color(0xFF1B3B20),
    secondary = EarthWarmBrown,
    onSecondary = Color.White,
    secondaryContainer = EarthWarmBrown.copy(alpha = 0.12f),
    onSecondaryContainer = Color(0xFF3D2E18),
    tertiary = EarthTerracotta,
    onTertiary = Color.White,
    tertiaryContainer = EarthTerracotta.copy(alpha = 0.12f),
    onTertiaryContainer = Color(0xFF5A2D1A),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFCBC4BA),
    outlineVariant = Color(0xFFE5DED5),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

// Dark Color Scheme (Earth & Wellness)
private val DarkColorScheme = darkColorScheme(
    primary = EarthLightSage,
    onPrimary = Color(0xFF1B3B20),
    primaryContainer = EarthSageGreen.copy(alpha = 0.25f),
    onPrimaryContainer = EarthLightSage,
    secondary = EarthSoftGold,
    onSecondary = Color(0xFF3D2E18),
    secondaryContainer = EarthWarmBrown.copy(alpha = 0.25f),
    onSecondaryContainer = EarthSoftGold,
    tertiary = EarthSoftTerracotta,
    onTertiary = Color(0xFF5A2D1A),
    tertiaryContainer = EarthTerracotta.copy(alpha = 0.25f),
    onTertiaryContainer = EarthSoftTerracotta,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF5A524A),
    outlineVariant = Color(0xFF443D36),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB)
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
