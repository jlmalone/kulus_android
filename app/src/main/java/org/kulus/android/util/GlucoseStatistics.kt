package org.kulus.android.util

import org.kulus.android.data.model.GlucoseReading
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * GlucoseStatistics - Calculate statistics from glucose readings
 *
 * Provides comprehensive analytics including:
 * - Average, min, max glucose values
 * - Standard deviation
 * - Time in range calculations
 * - A1C estimation
 * - Coefficient of variation
 */
data class GlucoseStatistics(
    val count: Int,
    val average: Double,
    val minimum: Double,
    val maximum: Double,
    val standardDeviation: Double,
    val coefficientOfVariation: Double,
    val timeInRange: TimeInRangeStats,
    val estimatedA1C: Double
) {
    companion object {
        /**
         * Calculate statistics from a list of glucose readings
         * All calculations assume mmol/L units
         */
        fun calculate(readings: List<GlucoseReading>): GlucoseStatistics? {
            if (readings.isEmpty()) return null

            // Convert all readings to mmol/L for consistent calculations
            val values = readings.map { reading ->
                when (reading.units) {
                    "mg/dL" -> reading.reading / 18.0 // Convert mg/dL to mmol/L
                    else -> reading.reading
                }
            }

            val count = values.size
            val average = values.average()
            val minimum = values.minOrNull() ?: 0.0
            val maximum = values.maxOrNull() ?: 0.0

            // Calculate standard deviation
            val variance = values.map { (it - average).pow(2) }.average()
            val standardDeviation = sqrt(variance)

            // Coefficient of variation (%)
            val coefficientOfVariation = if (average > 0) {
                (standardDeviation / average) * 100
            } else {
                0.0
            }

            // Time in range calculations
            val timeInRange = calculateTimeInRange(values)

            // A1C estimation using ADAG formula
            // A1C = (average glucose in mmol/L + 2.59) / 1.59
            val estimatedA1C = (average + 2.59) / 1.59

            return GlucoseStatistics(
                count = count,
                average = average,
                minimum = minimum,
                maximum = maximum,
                standardDeviation = standardDeviation,
                coefficientOfVariation = coefficientOfVariation,
                timeInRange = timeInRange,
                estimatedA1C = estimatedA1C
            )
        }

        /**
         * Calculate time in range statistics
         * Ranges (in mmol/L):
         * - Very Low: < 3.0
         * - Low: 3.0 - 3.9
         * - In Range: 3.9 - 10.0
         * - High: 10.0 - 13.9
         * - Very High: >= 13.9
         */
        private fun calculateTimeInRange(values: List<Double>): TimeInRangeStats {
            val total = values.size.toDouble()

            val veryLow = values.count { it < 3.0 }
            val low = values.count { it in 3.0..3.9 }
            val inRange = values.count { it in 3.9..10.0 }
            val high = values.count { it in 10.0..13.9 }
            val veryHigh = values.count { it >= 13.9 }

            return TimeInRangeStats(
                veryLowPercent = (veryLow / total) * 100,
                lowPercent = (low / total) * 100,
                inRangePercent = (inRange / total) * 100,
                highPercent = (high / total) * 100,
                veryHighPercent = (veryHigh / total) * 100,
                veryLowCount = veryLow,
                lowCount = low,
                inRangeCount = inRange,
                highCount = high,
                veryHighCount = veryHigh
            )
        }
    }
}

/**
 * Time in range statistics
 */
data class TimeInRangeStats(
    val veryLowPercent: Double,
    val lowPercent: Double,
    val inRangePercent: Double,
    val highPercent: Double,
    val veryHighPercent: Double,
    val veryLowCount: Int,
    val lowCount: Int,
    val inRangeCount: Int,
    val highCount: Int,
    val veryHighCount: Int
)

/**
 * Time range filter for chart display
 */
enum class TimeRange(val displayName: String, val days: Int) {
    DAY_1("24 Hours", 1),
    DAYS_7("7 Days", 7),
    DAYS_30("30 Days", 30),
    DAYS_90("90 Days", 90),
    YEAR_1("1 Year", 365);

    fun getStartTimestamp(): Long {
        val now = System.currentTimeMillis()
        return now - TimeUnit.DAYS.toMillis(days.toLong())
    }
}

/**
 * Extension function to filter readings by time range
 */
fun List<GlucoseReading>.filterByTimeRange(timeRange: TimeRange): List<GlucoseReading> {
    val startTime = timeRange.getStartTimestamp()
    return this.filter { it.timestamp >= startTime }
}

/**
 * Extension function to convert glucose value to display unit
 */
fun Double.toDisplayUnit(targetUnit: String, sourceUnit: String = "mmol/L"): Double {
    return when {
        sourceUnit == "mmol/L" && targetUnit == "mg/dL" -> this * 18.0
        sourceUnit == "mg/dL" && targetUnit == "mmol/L" -> this / 18.0
        else -> this
    }
}
