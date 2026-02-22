package org.kulus.android.data.api.v3

import com.google.gson.annotations.SerializedName

/**
 * Glucose measurement units
 */
enum class GlucoseUnits(val displayName: String, val apiValue: String) {
    @SerializedName("mmol/L")
    MMOL_L("mmol/L", "mmol/L"),

    @SerializedName("mg/dL")
    MG_DL("mg/dL", "mg/dL");

    companion object {
        // Conversion factor: 1 mmol/L = 18.0182 mg/dL
        const val CONVERSION_FACTOR = 18.0182

        /**
         * Convert mmol/L to mg/dL
         */
        fun mmolToMgdl(mmol: Double): Double = mmol * CONVERSION_FACTOR

        /**
         * Convert mg/dL to mmol/L
         */
        fun mgdlToMmol(mgdl: Double): Double = mgdl / CONVERSION_FACTOR

        /**
         * Parse units from string (case-insensitive)
         */
        fun fromString(value: String): GlucoseUnits = when (value.lowercase()) {
            "mmol/l", "mmol" -> MMOL_L
            "mg/dl", "mgdl", "mg" -> MG_DL
            else -> throw IllegalArgumentException("Unknown glucose units: $value")
        }

        /**
         * Try to parse units, returning null if not recognized
         */
        fun fromStringOrNull(value: String): GlucoseUnits? = try {
            fromString(value)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
