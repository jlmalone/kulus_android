package org.kulus.android.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.kulus.android.data.api.v3.*
import org.kulus.android.data.api.v3.dto.PostReadingRequest
import org.kulus.android.data.local.GlucoseReadingDao
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.data.preferences.PreferencesRepository
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Kulus v3 API operations.
 *
 * Key differences from v2:
 * - Uses phone number (E.164) instead of name for user identification
 * - No authentication tokens needed - just x-api-key header
 * - POST for adding readings instead of GET
 * - Simpler response format with status/data pattern
 */
@Singleton
class KulusV3Repository @Inject constructor(
    private val apiService: KulusV3ApiService,
    private val glucoseReadingDao: GlucoseReadingDao,
    private val preferencesRepository: PreferencesRepository
) {
    companion object {
        private const val TAG = "KulusV3Repository"
    }

    private val numberFormatter = DecimalFormat("0.##", DecimalFormatSymbols(Locale.US)).apply {
        maximumFractionDigits = 2
    }

    // Local data operations
    fun getAllReadingsLocal(): Flow<List<GlucoseReading>> {
        return glucoseReadingDao.getAllReadings()
    }

    suspend fun getReadingById(id: String): GlucoseReading? {
        return withContext(Dispatchers.IO) {
            glucoseReadingDao.getReadingById(id)
        }
    }

    /**
     * Get readings for the current user (by phone number)
     */
    fun getCurrentUserReadings(): Flow<List<GlucoseReading>> {
        return glucoseReadingDao.getAllReadings()
    }

    /**
     * Sync readings from the v3 API server
     */
    suspend fun syncReadingsFromServer(): Result<List<GlucoseReading>> = withContext(Dispatchers.IO) {
        try {
            val userPrefs = preferencesRepository.userPreferencesFlow.first()
            val phone = userPrefs.phoneNumber

            if (phone.isNullOrBlank()) {
                Log.w(TAG, "No phone number configured, skipping sync")
                return@withContext Result.failure(Exception("Phone number not configured"))
            }

            // Validate and normalize phone to E.164
            val e164Phone = when (val result = PhoneValidator.validate(phone)) {
                is PhoneValidator.Result.Valid -> result.e164Number
                is PhoneValidator.Result.Invalid -> {
                    Log.e(TAG, "Invalid phone number: ${result.reason}")
                    return@withContext Result.failure(Exception(result.reason))
                }
            }

            Log.d(TAG, "🔄 [SYNC] Fetching readings for phone: $e164Phone")

            val response = apiService.getReadings(userId = e164Phone, limit = 100)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                val readingsResponse = response.body()
                val readings = readingsResponse?.data?.readings?.mapNotNull { dto ->
                    try {
                        dto.toGlucoseReading()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse reading: ${e.message}")
                        null
                    }
                } ?: emptyList()

                Log.d(TAG, "✅ [SYNC] Fetched ${readings.size} readings from v3 API")

                // Save to local database
                glucoseReadingDao.insertReadings(readings)

                Result.success(readings)
            } else {
                val error = response.body()?.error?.message ?: response.message()
                Log.e(TAG, "❌ [SYNC] API error: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ [SYNC] Sync failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Add a new glucose reading
     */
    suspend fun addReading(
        reading: Double,
        units: GlucoseUnits = GlucoseUnits.MMOL_L,
        comment: String? = null,
        snackPass: Boolean = false,
        photoUri: String? = null,
        source: String = "android",
        tags: List<String> = emptyList()
    ): Result<GlucoseReading> = withContext(Dispatchers.IO) {
        try {
            // Validate reading value
            when (val validation = ReadingValidator.validate(reading, units)) {
                is ReadingValidator.Result.Invalid -> {
                    return@withContext Result.failure(Exception(validation.reason))
                }
                is ReadingValidator.Result.Valid -> { /* OK */ }
            }

            val userPrefs = preferencesRepository.userPreferencesFlow.first()
            val phone = userPrefs.phoneNumber

            if (phone.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Phone number not configured. Please complete onboarding."))
            }

            // Validate and normalize phone to E.164
            val e164Phone = when (val result = PhoneValidator.validate(phone)) {
                is PhoneValidator.Result.Valid -> result.e164Number
                is PhoneValidator.Result.Invalid -> {
                    return@withContext Result.failure(Exception(result.reason))
                }
            }

            // Classify the glucose level
            val mmolValue = when (units) {
                GlucoseUnits.MMOL_L -> reading
                GlucoseUnits.MG_DL -> GlucoseUnits.mgdlToMmol(reading)
            }
            val level = GlucoseLevel.classify(mmolValue)

            // Create local reading first
            val localReading = GlucoseReading(
                id = UUID.randomUUID().toString(),
                reading = reading,
                units = units.apiValue,
                name = e164Phone, // v3 uses phone as identifier
                comment = comment,
                snackPass = snackPass,
                source = source,
                timestamp = System.currentTimeMillis(),
                color = level.displayName,
                glucoseLevel = null,
                synced = false,
                photoUri = photoUri,
                tags = if (tags.isNotEmpty()) GlucoseReading.tagsToString(tags) else null
            )

            // Save locally first
            glucoseReadingDao.insertReading(localReading)

            // Try to sync to server
            try {
                val request = PostReadingRequest(
                    userId = e164Phone,
                    reading = reading,
                    units = units.apiValue,
                    timestamp = Instant.now().toString(),
                    source = source,
                    comment = comment,
                    snackPass = if (snackPass) true else null
                )

                val response = apiService.postReading(request)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d(TAG, "✅ Reading synced to v3 API")
                    val syncedReading = localReading.copy(synced = true)
                    glucoseReadingDao.updateReading(syncedReading)
                    Result.success(syncedReading)
                } else {
                    val error = response.body()?.error?.message ?: response.message()
                    Log.w(TAG, "⚠️ Sync failed, keeping local: $error")
                    Result.success(localReading)
                }
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Network error, keeping local: ${e.message}")
                Result.success(localReading)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to add reading: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Sync unsynced local readings to the server
     */
    suspend fun syncUnsyncedReadings(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val userPrefs = preferencesRepository.userPreferencesFlow.first()
            val phone = userPrefs.phoneNumber

            if (phone.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Phone number not configured"))
            }

            val e164Phone = PhoneValidator.normalizeOrThrow(phone)

            val unsyncedReadings = glucoseReadingDao.getUnsyncedReadings()
            var syncedCount = 0

            for (reading in unsyncedReadings) {
                try {
                    val request = PostReadingRequest(
                        userId = e164Phone,
                        reading = reading.reading,
                        units = reading.units,
                        timestamp = Instant.ofEpochMilli(reading.timestamp).toString(),
                        source = reading.source,
                        comment = reading.comment,
                        snackPass = if (reading.snackPass) true else null
                    )

                    val response = apiService.postReading(request)

                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        glucoseReadingDao.markAsSynced(reading.id)
                        syncedCount++
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to sync reading ${reading.id}: ${e.message}")
                    continue
                }
            }

            Log.d(TAG, "✅ Synced $syncedCount/${unsyncedReadings.size} readings")
            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to sync unsynced readings: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a reading locally
     */
    suspend fun deleteReading(reading: GlucoseReading): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            glucoseReadingDao.deleteReading(reading)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear all local readings
     */
    suspend fun clearAllReadings(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            glucoseReadingDao.deleteAllReadings()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
