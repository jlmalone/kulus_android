package org.kulus.android.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.kulus.android.data.api.v3.KulusV3ApiService
import org.kulus.android.data.api.v3.PhoneValidator
import org.kulus.android.data.api.v3.dto.PostReadingRequest
import org.kulus.android.data.local.GlucoseReadingDao
import org.kulus.android.data.preferences.PreferencesRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

/**
 * Enterprise-grade sync queue with exponential backoff.
 * Matches iOS SyncQueue.swift: cap 300s, max 10 retries, batch of 50.
 */
@Singleton
class SyncQueueManager @Inject constructor(
    private val glucoseReadingDao: GlucoseReadingDao,
    private val apiService: KulusV3ApiService,
    private val preferencesRepository: PreferencesRepository,
    private val apiLogger: ApiLogger
) {
    companion object {
        private const val TAG = "SyncQueueManager"
        private const val MAX_RETRIES = 10
        private const val BATCH_SIZE = 50
        private const val MAX_BACKOFF_SECONDS = 300L // 5 minutes cap
    }

    /**
     * Calculate exponential backoff delay: min(300s, 2^attempt)
     */
    private fun calculateBackoffMs(attemptCount: Int): Long {
        val backoffSeconds = min(MAX_BACKOFF_SECONDS, 2.0.pow(attemptCount).toLong())
        return backoffSeconds * 1000
    }

    /**
     * Check if a reading is eligible for retry based on backoff timing.
     */
    private fun isEligibleForRetry(lastAttempt: Long?, attemptCount: Int): Boolean {
        if (lastAttempt == null) return true
        val backoffMs = calculateBackoffMs(attemptCount)
        return System.currentTimeMillis() - lastAttempt >= backoffMs
    }

    /**
     * Process the pending sync queue. Returns count of successfully synced readings.
     */
    suspend fun processPendingQueue(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val userPrefs = preferencesRepository.userPreferencesFlow.first()
            val phone = userPrefs.phoneNumber

            if (phone.isNullOrBlank()) {
                Log.w(TAG, "No phone number configured, skipping sync queue")
                return@withContext Result.failure(Exception("Phone number not configured"))
            }

            val e164Phone = when (val result = PhoneValidator.validate(phone)) {
                is PhoneValidator.Result.Valid -> result.e164Number
                is PhoneValidator.Result.Invalid -> {
                    Log.e(TAG, "Invalid phone: ${result.reason}")
                    return@withContext Result.failure(Exception(result.reason))
                }
            }

            val pendingReadings = glucoseReadingDao.getPendingSyncReadings(MAX_RETRIES, BATCH_SIZE)
            if (pendingReadings.isEmpty()) {
                Log.d(TAG, "No pending readings to sync")
                return@withContext Result.success(0)
            }

            Log.d(TAG, "Processing ${pendingReadings.size} pending readings")
            var syncedCount = 0

            for (reading in pendingReadings) {
                // Check backoff eligibility
                if (!isEligibleForRetry(reading.lastSyncAttempt, reading.syncAttemptCount)) {
                    Log.d(TAG, "Skipping ${reading.id} — backoff not elapsed (attempt ${reading.syncAttemptCount})")
                    continue
                }

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
                        Log.d(TAG, "Synced ${reading.id}")
                    } else {
                        glucoseReadingDao.incrementSyncAttempt(reading.id)
                        Log.w(TAG, "Sync failed for ${reading.id}: ${response.body()?.error?.message ?: response.message()}")
                    }
                } catch (e: Exception) {
                    glucoseReadingDao.incrementSyncAttempt(reading.id)
                    Log.w(TAG, "Network error syncing ${reading.id}: ${e.message}")
                }
            }

            Log.d(TAG, "Sync queue processed: $syncedCount/${pendingReadings.size} synced")
            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Sync queue processing failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Get count of pending sync items.
     */
    suspend fun getPendingCount(): Int {
        return glucoseReadingDao.getPendingSyncCount()
    }
}
