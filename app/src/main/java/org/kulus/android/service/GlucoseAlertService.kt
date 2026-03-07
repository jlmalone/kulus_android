package org.kulus.android.service

import org.kulus.android.data.local.GlucoseReadingDao
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.data.model.GlucoseUnit
import org.kulus.android.data.preferences.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced glucose alert service providing:
 * - Rapid rise detection (>2.0 mmol/L within 30 minutes)
 * - Rapid fall detection (>2.0 mmol/L within 30 minutes)
 * - Rate of change monitoring (mmol/L per minute from recent readings)
 * - Configurable high/low thresholds
 * - Alert debouncing (no repeat of same alert type within cooldown period)
 */
@Singleton
class GlucoseAlertService @Inject constructor(
    private val glucoseReadingDao: GlucoseReadingDao,
    private val notificationService: NotificationService
) {

    companion object {
        /** Default rapid change threshold in mmol/L over 30 minutes */
        private const val RAPID_CHANGE_THRESHOLD_MMOL = 2.0

        /** Window in minutes to look back for rapid change detection */
        private const val RAPID_CHANGE_WINDOW_MINUTES = 30L

        /** How far back to look for readings when computing rate of change (2 hours) */
        private const val ANALYSIS_WINDOW_HOURS = 2L

        /** Minimum number of readings needed to compute rate of change */
        private const val MIN_READINGS_FOR_RATE = 2
    }

    /** Alert types for debouncing */
    enum class AlertType {
        HIGH,
        LOW,
        RAPID_RISE,
        RAPID_FALL
    }

    /** Tracks the last time each alert type was fired (epoch millis) */
    private val lastAlertTimes = mutableMapOf<AlertType, Long>()

    /**
     * Analyze a newly saved reading against recent history and fire appropriate alerts.
     *
     * @param reading The newly saved glucose reading
     * @param preferences Current user preferences (thresholds, enabled flags, cooldown)
     */
    suspend fun checkAlerts(reading: GlucoseReading, preferences: UserPreferences) {
        if (!preferences.localAlertsEnabled) return
        if (reading.snackPass) return

        val readingMmol = toMmol(reading.reading, reading.units)
        val now = System.currentTimeMillis()

        // Fetch recent readings for trend analysis (last 2 hours)
        val sinceTimestamp = now - (ANALYSIS_WINDOW_HOURS * 60 * 60 * 1000)
        val recentReadings = glucoseReadingDao.getReadingsSince(sinceTimestamp)

        // 1. High threshold alert
        if (readingMmol >= preferences.highAlertThreshold) {
            fireAlertIfCooldownExpired(AlertType.HIGH, preferences.alertCooldownMinutes, now) {
                notificationService.showHighAlert(reading.reading, reading.units)
            }
        }

        // 2. Low threshold alert
        if (readingMmol <= preferences.lowAlertThreshold) {
            fireAlertIfCooldownExpired(AlertType.LOW, preferences.alertCooldownMinutes, now) {
                notificationService.showLowAlert(reading.reading, reading.units)
            }
        }

        // Need at least 2 readings for rate analysis
        if (recentReadings.size < MIN_READINGS_FOR_RATE) return

        // 3. Rapid rise detection
        if (preferences.rapidRiseEnabled) {
            val rapidRise = detectRapidChange(recentReadings, reading, isRise = true)
            if (rapidRise != null) {
                fireAlertIfCooldownExpired(AlertType.RAPID_RISE, preferences.alertCooldownMinutes, now) {
                    notificationService.showRapidRiseAlert(
                        currentReading = reading.reading,
                        units = reading.units,
                        ratePerMinute = rapidRise
                    )
                }
            }
        }

        // 4. Rapid fall detection
        if (preferences.rapidFallEnabled) {
            val rapidFall = detectRapidChange(recentReadings, reading, isRise = false)
            if (rapidFall != null) {
                fireAlertIfCooldownExpired(AlertType.RAPID_FALL, preferences.alertCooldownMinutes, now) {
                    notificationService.showRapidFallAlert(
                        currentReading = reading.reading,
                        units = reading.units,
                        ratePerMinute = rapidFall
                    )
                }
            }
        }
    }

    /**
     * Detect rapid glucose change by comparing the current reading to readings
     * within the rapid change window (default 30 minutes).
     *
     * @return rate of change in mmol/L per minute if rapid change detected, null otherwise
     */
    private fun detectRapidChange(
        recentReadings: List<GlucoseReading>,
        currentReading: GlucoseReading,
        isRise: Boolean
    ): Double? {
        val currentMmol = toMmol(currentReading.reading, currentReading.units)
        val windowStart = currentReading.timestamp - (RAPID_CHANGE_WINDOW_MINUTES * 60 * 1000)

        // Get readings within the rapid change window
        val windowReadings = recentReadings.filter { r ->
            r.timestamp >= windowStart && r.timestamp < currentReading.timestamp && r.id != currentReading.id
        }

        if (windowReadings.isEmpty()) return null

        // Compare against the earliest reading in the window for maximum delta
        val comparisonReading = windowReadings.first()
        val comparisonMmol = toMmol(comparisonReading.reading, comparisonReading.units)

        val delta = currentMmol - comparisonMmol
        val timeDiffMinutes = (currentReading.timestamp - comparisonReading.timestamp).toDouble() / (60 * 1000)

        if (timeDiffMinutes <= 0) return null

        val ratePerMinute = delta / timeDiffMinutes

        return if (isRise && delta >= RAPID_CHANGE_THRESHOLD_MMOL) {
            ratePerMinute
        } else if (!isRise && delta <= -RAPID_CHANGE_THRESHOLD_MMOL) {
            ratePerMinute
        } else {
            null
        }
    }

    /**
     * Fire an alert only if the cooldown period for this alert type has expired.
     */
    private fun fireAlertIfCooldownExpired(
        alertType: AlertType,
        cooldownMinutes: Int,
        now: Long,
        action: () -> Unit
    ) {
        val lastFired = lastAlertTimes[alertType] ?: 0L
        val cooldownMillis = cooldownMinutes * 60 * 1000L

        if (now - lastFired >= cooldownMillis) {
            action()
            lastAlertTimes[alertType] = now
        }
    }

    /**
     * Convert a reading value to mmol/L for consistent threshold comparisons.
     */
    private fun toMmol(value: Double, units: String): Double {
        return if (units.equals(GlucoseUnit.MG_DL.apiValue, ignoreCase = true)) {
            value / 18.0
        } else {
            value
        }
    }
}
