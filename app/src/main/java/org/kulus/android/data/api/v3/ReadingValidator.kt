package org.kulus.android.data.api.v3

/**
 * Validates glucose reading values
 * Valid range: 0.1 - 50 mmol/L (approximately 2 - 900 mg/dL)
 */
object ReadingValidator {
    // Valid range in mmol/L
    const val MIN_MMOL = 0.1
    const val MAX_MMOL = 50.0

    // Equivalent range in mg/dL
    val MIN_MGDL = GlucoseUnits.mmolToMgdl(MIN_MMOL)
    val MAX_MGDL = GlucoseUnits.mmolToMgdl(MAX_MMOL)

    /**
     * Validation result
     */
    sealed class Result {
        data class Valid(val value: Double, val units: GlucoseUnits) : Result()
        data class Invalid(val reason: String) : Result()
    }

    /**
     * Validate a glucose reading value
     * @param value The glucose value
     * @param units The units of measurement
     * @return Result indicating success or failure with reason
     */
    fun validate(value: Double, units: GlucoseUnits): Result {
        if (value.isNaN() || value.isInfinite()) {
            return Result.Invalid("Reading value must be a valid number")
        }

        val mmolValue = when (units) {
            GlucoseUnits.MMOL_L -> value
            GlucoseUnits.MG_DL -> GlucoseUnits.mgdlToMmol(value)
        }

        return when {
            mmolValue < MIN_MMOL -> {
                val displayMin = if (units == GlucoseUnits.MMOL_L) "$MIN_MMOL mmol/L" else "${"%.0f".format(MIN_MGDL)} mg/dL"
                Result.Invalid("Reading too low. Minimum is $displayMin")
            }
            mmolValue > MAX_MMOL -> {
                val displayMax = if (units == GlucoseUnits.MMOL_L) "$MAX_MMOL mmol/L" else "${"%.0f".format(MAX_MGDL)} mg/dL"
                Result.Invalid("Reading too high. Maximum is $displayMax")
            }
            else -> Result.Valid(value, units)
        }
    }

    /**
     * Validate or throw exception
     */
    fun validateOrThrow(value: Double, units: GlucoseUnits): Double {
        return when (val result = validate(value, units)) {
            is Result.Valid -> result.value
            is Result.Invalid -> throw IllegalArgumentException(result.reason)
        }
    }

    /**
     * Check if a reading is in a dangerous range (hypo or severe hyper)
     */
    fun isDangerous(mmolValue: Double): Boolean {
        return mmolValue < 3.0 || mmolValue > 20.0
    }
}
