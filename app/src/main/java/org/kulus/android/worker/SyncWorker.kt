package org.kulus.android.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.kulus.android.data.repository.KulusV3Repository

/**
 * Background worker for syncing glucose readings with the v3 API server
 *
 * Features:
 * - Periodic sync every 30-60 minutes
 * - Syncs when network connectivity is available
 * - Exponential backoff on failure (max 3 retries)
 * - Downloads readings from server (by phone number)
 * - Uploads unsynced local readings
 * - Cross-platform compatible with iOS (both use v3 API with phone numbers)
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: KulusV3Repository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "🔄 Starting v3 API sync...")

            // First, sync unsynced readings to server
            val uploadResult = repository.syncUnsyncedReadings()
            uploadResult.onSuccess { count ->
                Log.d(TAG, "📤 Uploaded $count unsynced readings")
            }

            // Then, download latest readings from server
            val downloadResult = repository.syncReadingsFromServer()

            if (downloadResult.isSuccess) {
                val readings = downloadResult.getOrNull() ?: emptyList()
                Log.d(TAG, "✅ Sync complete - downloaded ${readings.size} readings")
                Result.success()
            } else {
                val error = downloadResult.exceptionOrNull()?.message ?: "Unknown error"
                Log.w(TAG, "⚠️ Sync failed: $error")
                // Check if we should retry
                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Sync exception: ${e.message}", e)
            // Network or other errors - retry if under max attempts
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "kulus_sync"
        const val MAX_RETRIES = 3
    }
}
