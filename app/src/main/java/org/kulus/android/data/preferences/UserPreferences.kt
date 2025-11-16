package org.kulus.android.data.preferences

import org.kulus.android.data.model.GlucoseUnit

/**
 * User preferences for the Kulus Android app
 */
data class UserPreferences(
    val defaultName: String = "mobile-user",
    val preferredUnit: GlucoseUnit = GlucoseUnit.MMOL_L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val openAiApiKey: String? = null,
    val targetRangeLow: Double = 3.9,  // mmol/L
    val targetRangeHigh: Double = 7.8  // mmol/L
)

enum class ThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark");

    companion object {
        fun fromOrdinal(ordinal: Int): ThemeMode {
            return values().getOrNull(ordinal) ?: SYSTEM
        }
    }
}
