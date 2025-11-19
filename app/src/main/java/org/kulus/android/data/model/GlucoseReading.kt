package org.kulus.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "glucose_readings")
data class GlucoseReading(
    @PrimaryKey
    val id: String,
    val reading: Double,
    val units: String = "mmol/L",
    val name: String,
    val comment: String? = null,
    val snackPass: Boolean = false,
    val source: String = "manual",
    val timestamp: Long = System.currentTimeMillis(),
    val color: String? = null,
    val glucoseLevel: Int? = null,
    val synced: Boolean = false,
    val photoUri: String? = null,  // URI of photo associated with reading
    val tags: String? = null,  // Comma-separated tags (e.g., "fasting,morning,pre-meal")
    val profileId: String = "00000000-0000-0000-0000-000000000001"  // FK to user_profiles table
) {
    fun getTagsList(): List<String> {
        return tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    companion object {
        fun tagsToString(tagsList: List<String>): String {
            return tagsList.filter { it.isNotBlank() }.joinToString(",")
        }
    }
}

data class KulusTimestamp(
    @SerializedName("_seconds")
    val seconds: Long? = null,
    @SerializedName("_nanoseconds")
    val nanoseconds: Long? = null,
    @SerializedName("seconds")
    val altSeconds: Long? = null,
    @SerializedName("nanoseconds")
    val altNanoseconds: Long? = null
) {
    fun toDate(): Date {
        val sec = seconds ?: altSeconds ?: 0L
        val nano = nanoseconds ?: altNanoseconds ?: 0L
        return Date(sec * 1000 + nano / 1_000_000)
    }
}

data class KulusReadingDTO(
    val id: String?,
    val readingId: String?,
    val reading: Double?,
    val units: String?,
    val name: String?,
    val comment: String?,
    val snackPass: Boolean?,
    val source: String?,
    val timestamp: KulusTimestamp?,
    val ts: KulusTimestamp?,
    val color: String?,
    val glucoseLevel: GlucoseLevelDetails?
) {
    data class GlucoseLevelDetails(
        val glucoseLevel: Double?,
        val color: String?
    )

    fun toGlucoseReading(): GlucoseReading {
        val actualTimestamp = timestamp ?: ts
        return GlucoseReading(
            id = id ?: readingId ?: java.util.UUID.randomUUID().toString(),
            reading = reading ?: glucoseLevel?.glucoseLevel ?: 0.0,
            units = units ?: "mmol/L",
            name = name ?: "",
            comment = comment,
            snackPass = snackPass ?: false,
            source = source ?: "manual",
            timestamp = actualTimestamp?.toDate()?.time ?: System.currentTimeMillis(),
            color = color ?: glucoseLevel?.color,
            glucoseLevel = glucoseLevel?.glucoseLevel?.toInt(),
            synced = true,
            photoUri = null  // Server readings don't have local photos
        )
    }
}

data class KulusReadingsResponse(
    val result: String?,
    val totalReadings: Int?,
    val readings: List<KulusReadingDTO>
)

data class AddReadingResponse(
    val result: String,
    val message: String,
    val data: ReadingData?
) {
    data class ReadingData(
        val id: String,
        val name: String,
        val reading: Double,
        val units: String
    )
}

data class AuthRequest(
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val expiresIn: Long
)

data class VerifyTokenRequest(
    val token: String
)

data class VerifyTokenResponse(
    val valid: Boolean
)

enum class GlucoseUnit(val displayName: String, val apiValue: String) {
    MMOL_L("mmol/L", "mmol/L"),
    MG_DL("mg/dL", "mg/dL");

    companion object {
        fun fromString(value: String): GlucoseUnit {
            return values().find { it.apiValue.equals(value, ignoreCase = true) } ?: MMOL_L
        }
    }
}
