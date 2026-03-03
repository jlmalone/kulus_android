package org.kulus.android.data.api.v3.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.kulus.android.data.api.v3.GlucoseLevel
import org.kulus.android.data.api.v3.GlucoseUnits
import org.kulus.android.data.model.GlucoseReading
import java.time.Instant
import java.util.UUID

/**
 * Response from GET /readings endpoint
 * Actual format: { "status": "success", "data": { ... } }
 */
data class ReadingsResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: ReadingsDataDto? = null,

    @SerializedName("error")
    val error: ErrorDto? = null
) {
    val isSuccess: Boolean get() = status == "success"
}

data class ReadingsDataDto(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("readings")
    val readings: List<ReadingDto>,

    @SerializedName("stats")
    val stats: StatsDto? = null,

    @SerializedName("pagination")
    val pagination: PaginationDto? = null
)

data class ErrorDto(
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("details")
    val details: Map<String, String>? = null,

    @SerializedName("requestId")
    val requestId: String? = null
)

/**
 * Individual reading in response
 */
data class ReadingDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("reading")
    val reading: Double, // API uses "reading" not "value"

    @SerializedName("units")
    val units: String,

    @SerializedName("timestamp")
    val timestamp: String, // ISO8601 string, e.g. "2026-01-23T00:46:52.441Z"

    // glucoseLevel can be a String ("Green") or Object {"glucoseLevel":5,"color":"Green"}
    @SerializedName("glucoseLevel")
    val glucoseLevel: JsonElement? = null,

    @SerializedName("comment")
    val comment: String? = null, // API uses "comment" not "notes"

    @SerializedName("snackPass")
    val snackPass: Boolean? = null,

    @SerializedName("submittedBy")
    val submittedBy: String? = null,

    @SerializedName("source")
    val source: String? = null
) {
    fun toGlucoseReading(): GlucoseReading {
        val parsedUnits = try {
            GlucoseUnits.fromString(units)
        } catch (e: IllegalArgumentException) {
            GlucoseUnits.MMOL_L // Default
        }

        // Parse ISO8601 timestamp to epoch millis
        val epochMillis = try {
            Instant.parse(timestamp).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }

        // Classify level based on reading value
        val mmolValue = when (parsedUnits) {
            GlucoseUnits.MMOL_L -> reading
            GlucoseUnits.MG_DL -> GlucoseUnits.mgdlToMmol(reading)
        }

        // Extract level string from String or Object {"color":"Green"} format
        val levelString: String? = when {
            glucoseLevel == null || glucoseLevel.isJsonNull -> null
            glucoseLevel.isJsonPrimitive -> glucoseLevel.asString
            glucoseLevel.isJsonObject -> glucoseLevel.asJsonObject.get("color")?.asString
            else -> null
        }
        val level = levelString?.let { GlucoseLevel.fromString(it) }
            ?: GlucoseLevel.classify(mmolValue)

        return GlucoseReading(
            id = id,
            reading = reading,
            units = parsedUnits.apiValue,
            name = submittedBy ?: "", // v3 uses phone as userId
            comment = comment,
            snackPass = snackPass ?: false,
            source = source ?: "v3",
            timestamp = epochMillis,
            color = level.displayName,
            glucoseLevel = null, // Not needed for v3
            synced = true // Coming from server
        )
    }
}

/**
 * Statistics in response
 */
data class StatsDto(
    @SerializedName("count")
    val count: Int,

    @SerializedName("average")
    val average: Double? = null,

    @SerializedName("min")
    val min: Double? = null,

    @SerializedName("max")
    val max: Double? = null,

    @SerializedName("trend")
    val trend: String? = null
)

/**
 * Pagination info
 */
data class PaginationDto(
    @SerializedName("offset")
    val offset: Int = 0,

    @SerializedName("limit")
    val limit: Int = 10,

    @SerializedName("total")
    val total: Int = 0
)

/**
 * Response from POST /readings endpoint
 */
data class PostReadingResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: ReadingDto? = null,

    @SerializedName("error")
    val error: ErrorDto? = null,

    @SerializedName("message")
    val message: String? = null
) {
    val isSuccess: Boolean get() = status == "success"
}
