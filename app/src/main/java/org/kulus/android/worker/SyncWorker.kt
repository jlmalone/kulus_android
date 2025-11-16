package org.kulus.android.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.kulus.android.data.repository.KulusRepository

/**
 * Background worker for syncing glucose readings with the server
 *
 * Features:
 * - Periodic sync every 30-60 minutes
 * - Syncs when network connectivity is available
 * - Exponential backoff on failure (max 3 retries)
 * - Downloads readings from server
 * - Uploads unsynced local readings
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: KulusRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // First, sync unsynced readings to server
            repository.syncUnsyncedReadings()

            // Then, download latest readings from server
            repository.syncReadingsFromServer()
                .onSuccess {
                    // Success - all readings synced
                    Result.success()
                }
                .onFailure { error ->
                    // Check if we should retry
                    if (runAttemptCount < MAX_RETRIES) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
        } catch (e: Exception) {
            // Network or other errors - retry if under max attempts
            if (runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        const val WORK_NAME = "kulus_sync"
        const val MAX_RETRIES = 3
    }
}
