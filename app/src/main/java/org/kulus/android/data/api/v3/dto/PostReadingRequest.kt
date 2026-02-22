package org.kulus.android.data.api.v3.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /readings endpoint
 */
data class PostReadingRequest(
    @SerializedName("userId")
    val userId: String, // E.164 phone format required (e.g., +15109842762)

    @SerializedName("reading")
    val reading: Double,

    @SerializedName("units")
    val units: String = "mmol/L", // "mmol/L" or "mg/dL"

    @SerializedName("timestamp")
    val timestamp: String? = null, // ISO 8601 format (e.g., "2026-01-19T14:30:00Z"), omit for server time

    @SerializedName("source")
    val source: String? = null, // Optional: "manual", "meter", "cgm", "android"

    @SerializedName("comment")
    val comment: String? = null, // Optional notes, max 500 chars

    @SerializedName("snackPass")
    val snackPass: Boolean? = null // Skip SMS alert if true
)
