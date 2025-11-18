package org.kulus.android.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import org.kulus.android.BuildConfig
import org.kulus.android.data.api.KulusApiService
import org.kulus.android.data.local.GlucoseReadingDao
import org.kulus.android.data.local.TokenStore
import org.kulus.android.data.model.*
import org.kulus.android.data.preferences.PreferencesRepository
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KulusRepository @Inject constructor(
    private val apiService: KulusApiService,
    private val glucoseReadingDao: GlucoseReadingDao,
    private val tokenStore: TokenStore,
    private val preferencesRepository: PreferencesRepository
) {

    private val numberFormatter = DecimalFormat("0.#", DecimalFormatSymbols(Locale.US)).apply {
        maximumFractionDigits = 2
    }

    // Local data operations
    fun getAllReadingsLocal(): Flow<List<GlucoseReading>> {
        return glucoseReadingDao.getAllReadings()
    }

    fun getReadingsByName(name: String): Flow<List<GlucoseReading>> {
        return glucoseReadingDao.getReadingsByName(name)
    }

    /**
     * Get readings for the current user only.
     * This implements client-side data segregation matching iOS behavior.
     * CRITICAL: Always use this method instead of getAllReadingsLocal() to prevent
     * users from seeing each other's glucose data.
     */
    fun getCurrentUserReadings(): Flow<List<GlucoseReading>> {
        return preferencesRepository.userPreferencesFlow
            .flatMapLatest { userPrefs ->
                val userName = userPrefs.defaultName
                android.util.Log.d("KulusRepository", "🔒 [LOCAL] Filtering readings for user: $userName")
                glucoseReadingDao.getReadingsByName(userName)
            }
    }

    suspend fun getReadingById(id: String): GlucoseReading? {
        return withContext(Dispatchers.IO) {
            glucoseReadingDao.getReadingById(id)
        }
    }

    // Authentication
    suspend fun authenticate(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.authenticate(
                AuthRequest(password = BuildConfig.API_PASSWORD)
            )

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true) {
                    tokenStore.saveToken(authResponse.token, authResponse.expiresIn)
                    Result.success(true)
                } else {
                    Result.failure(Exception("Authentication failed"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureAuthenticated(): Result<Unit> = withContext(Dispatchers.IO) {
        val isValid = tokenStore.isTokenValid().first()
        if (!isValid) {
            authenticate().map { }
        } else {
            Result.success(Unit)
        }
    }

    // Remote data operations
    // CRITICAL PRIVACY FIX: Only sync readings for current user
    suspend fun syncReadingsFromServer(): Result<List<GlucoseReading>> = withContext(Dispatchers.IO) {
        try {
            ensureAuthenticated().getOrThrow()

            // Get current user's name from preferences
            val userPrefs = preferencesRepository.userPreferencesFlow.first()
            val userName = userPrefs.defaultName

            android.util.Log.d("KulusRepository", "🔒 [SYNC] Fetching readings for user: $userName")

            // Use filtered endpoint instead of getAllReadings
            val response = apiService.getReadingsByName(name = userName)
            if (response.isSuccessful) {
                val readingsResponse = response.body()
                val readings = readingsResponse?.readings?.mapNotNull { dto ->
                    try {
                        dto.toGlucoseReading()
                    } catch (e: Exception) {
                        android.util.Log.w("KulusRepository", "Failed to parse reading: ${e.message}")
                        null // Skip invalid readings
                    }
                } ?: emptyList()

                android.util.Log.d("KulusRepository", "✅ [SYNC] Fetched ${readings.size} readings from Kulus for $userName")

                // Save to local database
                glucoseReadingDao.insertReadings(readings)

                Result.success(readings)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("KulusRepository", "❌ [SYNC] Sync failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun addReading(
        reading: Double,
        name: String,
        units: GlucoseUnit = GlucoseUnit.MMOL_L,
        comment: String? = null,
        snackPass: Boolean = false,
        photoUri: String? = null,
        source: String = "android",
        tags: List<String> = emptyList()
    ): Result<GlucoseReading> = withContext(Dispatchers.IO) {
        try {
            // Create local reading first
            val localReading = GlucoseReading(
                id = UUID.randomUUID().toString(),
                reading = reading,
                units = units.apiValue,
                name = name,
                comment = comment,
                snackPass = snackPass,
                source = source,
                timestamp = System.currentTimeMillis(),
                synced = false,
                photoUri = photoUri,
                tags = if (tags.isNotEmpty()) GlucoseReading.tagsToString(tags) else null
            )

            // Save locally
            glucoseReadingDao.insertReading(localReading)

            // Try to sync to server
            try {
                ensureAuthenticated().getOrThrow()

                val formattedValue = numberFormatter.format(reading)
                val response = apiService.addReading(
                    name = name,
                    reading = formattedValue,
                    units = units.apiValue,
                    comment = comment,
                    snackPass = snackPass,
                    source = source
                )

                if (response.isSuccessful) {
                    // Mark as synced
                    val syncedReading = localReading.copy(synced = true)
                    glucoseReadingDao.updateReading(syncedReading)
                    Result.success(syncedReading)
                } else {
                    // Return local reading even if sync failed
                    Result.success(localReading)
                }
            } catch (e: Exception) {
                // Return local reading if network fails
                Result.success(localReading)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncUnsyncedReadings(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            ensureAuthenticated().getOrThrow()

            val unsyncedReadings = glucoseReadingDao.getUnsyncedReadings()
            var syncedCount = 0

            for (reading in unsyncedReadings) {
                try {
                    val formattedValue = numberFormatter.format(reading.reading)
                    val response = apiService.addReading(
                        name = reading.name,
                        reading = formattedValue,
                        units = reading.units,
                        comment = reading.comment,
                        snackPass = reading.snackPass,
                        source = reading.source
                    )

                    if (response.isSuccessful) {
                        glucoseReadingDao.markAsSynced(reading.id)
                        syncedCount++
                    }
                } catch (e: Exception) {
                    // Continue to next reading
                    continue
                }
            }

            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReading(reading: GlucoseReading): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            glucoseReadingDao.deleteReading(reading)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllReadings(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            glucoseReadingDao.deleteAllReadings()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
