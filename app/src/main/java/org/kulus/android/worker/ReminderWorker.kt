package org.kulus.android.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.kulus.android.service.NotificationService
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationService: NotificationService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val message = inputData.getString(KEY_MESSAGE) ?: "Time to check your glucose level"

            // Show the reminder notification
            notificationService.showTestingReminder(message)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "glucose_reminder"
        private const val KEY_MESSAGE = "message"

        fun scheduleReminder(
            context: Context,
            hourOfDay: Int,
            minute: Int,
            message: String = "Time to check your glucose level"
        ) {
            // Calculate delay until the next occurrence of this time
            val currentTime = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hourOfDay)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)

                // If the time has already passed today, schedule for tomorrow
                if (timeInMillis <= currentTime) {
                    add(java.util.Calendar.DAY_OF_MONTH, 1)
                }
            }

            val delay = calendar.timeInMillis - currentTime

            val inputData = Data.Builder()
                .putString(KEY_MESSAGE, message)
                .build()

            val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
                24, TimeUnit.HOURS  // Repeat daily
            )
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "$WORK_NAME\_$hourOfDay\_$minute",
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest
            )
        }

        fun cancelReminder(context: Context, hourOfDay: Int, minute: Int) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("$WORK_NAME\_$hourOfDay\_$minute")
        }

        fun cancelAllReminders(context: Context) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag(WORK_NAME)
        }
    }
}
