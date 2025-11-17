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
    val targetRangeHigh: Double = 7.8,  // mmol/L

    // Onboarding data
    val onboardingCompleted: Boolean = false,
    val phoneNumber: String? = null,
    val selectedDeviceType: DeviceType = DeviceType.CONTOUR_NEXT_ONE,
    val smsAlertsEnabled: Boolean = true,

    // Notification preferences
    val localAlertsEnabled: Boolean = true,
    val criticalLowThreshold: Double = 3.0,    // mmol/L (~54 mg/dL)
    val criticalHighThreshold: Double = 13.9   // mmol/L (~250 mg/dL)
)

enum class DeviceType(val displayName: String) {
    CONTOUR_NEXT_ONE("Contour Next One (Recommended)"),
    OTHER("Other Meter");

    companion object {
        fun fromOrdinal(ordinal: Int): DeviceType {
            return values().getOrNull(ordinal) ?: CONTOUR_NEXT_ONE
        }
    }
}

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
