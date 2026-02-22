package org.kulus.android.data.api.v3

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil

/**
 * Validates and formats phone numbers to E.164 format
 * E.164 format: +[country code][subscriber number]
 * Example: +16464849595
 */
object PhoneValidator {
    private val phoneUtil = PhoneNumberUtil.getInstance()

    /**
     * Validation result
     */
    sealed class Result {
        data class Valid(val e164Number: String) : Result()
        data class Invalid(val reason: String) : Result()
    }

    /**
     * Validate and convert phone number to E.164 format
     * @param phone The phone number to validate
     * @param defaultRegion Default region code (e.g., "US") for numbers without country code
     * @return Result indicating success with E.164 number or failure with reason
     */
    fun validate(phone: String, defaultRegion: String = "US"): Result {
        if (phone.isBlank()) {
            return Result.Invalid("Phone number cannot be empty")
        }

        return try {
            val parsedNumber = phoneUtil.parse(phone, defaultRegion)

            if (!phoneUtil.isValidNumber(parsedNumber)) {
                return Result.Invalid("Invalid phone number format")
            }

            val e164 = phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
            Result.Valid(e164)
        } catch (e: NumberParseException) {
            Result.Invalid("Could not parse phone number: ${e.message}")
        }
    }

    /**
     * Check if a phone number is already in E.164 format
     */
    fun isE164Format(phone: String): Boolean {
        return phone.matches(Regex("^\\+[1-9]\\d{1,14}$"))
    }

    /**
     * Normalize phone to E.164 or throw exception
     */
    fun normalizeOrThrow(phone: String, defaultRegion: String = "US"): String {
        return when (val result = validate(phone, defaultRegion)) {
            is Result.Valid -> result.e164Number
            is Result.Invalid -> throw IllegalArgumentException(result.reason)
        }
    }

    /**
     * Normalize phone to E.164 or return null
     */
    fun normalizeOrNull(phone: String, defaultRegion: String = "US"): String? {
        return when (val result = validate(phone, defaultRegion)) {
            is Result.Valid -> result.e164Number
            is Result.Invalid -> null
        }
    }
}
