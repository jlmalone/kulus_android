package org.kulus.android.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.kulus.android.data.repository.KulusV3Repository
import org.kulus.android.service.SyncQueueManager

/**
 * Background worker for syncing glucose readings with the v3 API server.
 * Uses enterprise-grade SyncQueueManager with exponential backoff (cap 300s, max 10 retries).
 * Matches iOS SyncQueue behavior.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: KulusV3Repository,
    private val syncQueueManager: SyncQueueManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting sync...")

            // Process pending queue with enterprise backoff
            val uploadResult = syncQueueManager.processPendingQueue()
            uploadResult.onSuccess { count ->
                Log.d(TAG, "Uploaded $count readings from sync queue")
            }

            // Download latest readings from server
            val downloadResult = repository.syncReadingsFromServer()

            if (downloadResult.isSuccess) {
                val readings = downloadResult.getOrNull() ?: emptyList()
                Log.d(TAG, "Sync complete — downloaded ${readings.size} readings")
                Result.success()
            } else {
                val error = downloadResult.exceptionOrNull()?.message ?: "Unknown error"
                Log.w(TAG, "Sync failed: $error")
                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync exception: ${e.message}", e)
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
