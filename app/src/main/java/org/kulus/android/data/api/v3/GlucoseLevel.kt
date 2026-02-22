package org.kulus.android.data.api.v3

import com.google.gson.annotations.SerializedName

/**
 * Glucose level classification
 * Green: Normal/Target range
 * Orange: Elevated but not critical
 * Red: High - needs attention
 * Purple: Critical hypoglycemia
 */
enum class GlucoseLevel(val displayName: String, val description: String) {
    @SerializedName("green")
    GREEN("Green", "Normal/Target range"),

    @SerializedName("orange")
    ORANGE("Orange", "Elevated"),

    @SerializedName("red")
    RED("Red", "High - needs attention"),

    @SerializedName("purple")
    PURPLE("Purple", "Critical hypoglycemia");

    companion object {
        /**
         * Classify a glucose reading in mmol/L
         * Thresholds based on standard diabetes care guidelines:
         * - Purple (hypo): < 3.9 mmol/L (< 70 mg/dL)
         * - Green (normal): 3.9 - 7.8 mmol/L (70 - 140 mg/dL)
         * - Orange (elevated): 7.8 - 11.1 mmol/L (140 - 200 mg/dL)
         * - Red (high): > 11.1 mmol/L (> 200 mg/dL)
         */
        fun classify(mmolL: Double): GlucoseLevel = when {
            mmolL < 3.9 -> PURPLE
            mmolL <= 7.8 -> GREEN
            mmolL <= 11.1 -> ORANGE
            else -> RED
        }

        /**
         * Parse level from string (case-insensitive)
         */
        fun fromString(value: String): GlucoseLevel? = when (value.lowercase()) {
            "green" -> GREEN
            "orange" -> ORANGE
            "red" -> RED
            "purple" -> PURPLE
            else -> null
        }
    }
}
