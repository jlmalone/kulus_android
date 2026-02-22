package org.kulus.android.ui.theme

import androidx.compose.ui.graphics.Color

// iOS-Style Adaptive Colors (Light Mode)
val LightBackground = Color(0xFFF2F2F7)  // iOS systemBackground
val LightSecondaryBackground = Color(0xFFFFFFFF)  // iOS secondarySystemBackground
val LightSurface = Color(0xFFFFFFFF)  // Card backgrounds
val LightSurfaceVariant = Color(0xFFF2F2F7)  // systemGray6
val LightTextPrimary = Color(0xFF000000)  // label
val LightTextSecondary = Color(0xFF8E8E93)  // secondaryLabel

// iOS-Style Adaptive Colors (Dark Mode)
val DarkBackground = Color(0xFF000000)  // iOS systemBackground dark
val DarkSecondaryBackground = Color(0xFF1C1C1E)  // iOS secondarySystemBackground dark
val DarkSurface = Color(0xFF1C1C1E)  // Card backgrounds
val DarkSurfaceVariant = Color(0xFF2C2C2E)  // systemGray6 dark
val DarkTextPrimary = Color(0xFFFFFFFF)  // label dark
val DarkTextSecondary = Color(0xFF8E8E93)  // secondaryLabel dark

// Brand Colors (matching iOS)
val BrandGreen = Color(0xFF33B249)  // Kulus green
val BrandMint = Color(0xFF33E6CC)  // Kulus mint
val BrandBlue = Color(0xFF007AFF)  // iOS blue

// Glucose Level Colors - WCAG AA compliant (matching iOS)
val GlucoseLow = Color(0xFFCC0000)  // Dark red for low (purple in original)
val GlucoseNormal = Color(0xFF008000)  // Dark green for normal
val GlucoseElevated = Color(0xFFE68000)  // Dark orange for elevated
val GlucoseHigh = Color(0xFFCC0000)  // Dark red for high

// Legacy Matrix Colors (kept for compatibility)
val MatrixBackground = Color(0xFF020B09)
val MatrixPanelStart = Color(0xFF031413)
val MatrixPanelEnd = Color(0xFF061F18)
val MatrixNeon = Color(0xFF00FFB8)
val MatrixNeonDim = Color(0x9900FFB8)
val MatrixAmber = Color(0xFFFFC833)
val MatrixTextPrimary = Color(0xFFB8FFE6)
val MatrixTextSecondary = Color(0x99B8FFE6)

// Legacy Glucose Colors (for backward compatibility)
val GlucoseGreen = GlucoseNormal
val GlucoseOrange = GlucoseElevated
val GlucoseRed = GlucoseHigh
val GlucoseCrimson = GlucoseLow  // Purple/Critical
