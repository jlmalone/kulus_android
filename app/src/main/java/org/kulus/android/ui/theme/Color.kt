package org.kulus.android.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Earth & Wellness Theme - Kulus Glucose Monitoring
// Warm, grounded palette inspired by nature and wellness
// ============================================================

// --- Light Mode ---
val LightBackground = Color(0xFFFBF8F4)       // Warm cream
val LightSecondaryBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0EDE8)    // Light warm gray
val LightTextPrimary = Color(0xFF2C2417)       // Dark warm brown
val LightTextSecondary = Color(0xFF7A6E5D)     // Muted brown

// --- Dark Mode ---
val DarkBackground = Color(0xFF1A1714)         // Deep earth brown
val DarkSecondaryBackground = Color(0xFF2A2420) // Dark bark
val DarkSurface = Color(0xFF2A2420)            // Dark bark
val DarkSurfaceVariant = Color(0xFF3A332C)     // Dark moss
val DarkTextPrimary = Color(0xFFF5EDE4)        // Warm white
val DarkTextSecondary = Color(0xFFA89B8A)      // Muted sand

// --- Brand / Earth Colors ---
val EarthSageGreen = Color(0xFF6B8F71)         // Primary: calming sage
val EarthWarmBrown = Color(0xFF8B6F47)         // Secondary: grounded brown
val EarthTerracotta = Color(0xFFC2704E)        // Tertiary: warm accent
val EarthSoftGold = Color(0xFFD4A96A)          // Dark-mode secondary warmth
val EarthLightSage = Color(0xFFA8C5AD)         // Dark-mode primary sage
val EarthSoftTerracotta = Color(0xFFD99B7C)    // Dark-mode tertiary

// --- Glucose Level Colors (WCAG AA compliant — DO NOT CHANGE) ---
val GlucoseLow = Color(0xFFCC0000)             // Dark red for low
val GlucoseNormal = Color(0xFF008000)          // Dark green for normal
val GlucoseElevated = Color(0xFFE68000)        // Dark orange for elevated
val GlucoseHigh = Color(0xFFCC0000)            // Dark red for high

// Legacy aliases (for backward compatibility in GlucoseReadingCard, etc.)
val GlucoseGreen = GlucoseNormal
val GlucoseOrange = GlucoseElevated
val GlucoseRed = GlucoseHigh
val GlucoseCrimson = GlucoseLow
