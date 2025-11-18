package org.kulus.android.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import org.kulus.android.data.preferences.PreferencesRepository
import org.kulus.android.worker.ReminderWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) {

    suspend fun updateReminders() {
        val preferences = preferencesRepository.userPreferencesFlow.first()

        // Cancel all existing reminders first
        ReminderWorker.cancelAllReminders(context)

        if (!preferences.remindersEnabled) {
            return
        }

        // Schedule morning reminder if enabled
        if (preferences.morningReminderEnabled) {
            ReminderWorker.scheduleReminder(
                context = context,
                hourOfDay = preferences.morningReminderHour,
                minute = preferences.morningReminderMinute,
                message = "Morning glucose check"
            )
        }

        // Schedule evening reminder if enabled
        if (preferences.eveningReminderEnabled) {
            ReminderWorker.scheduleReminder(
                context = context,
                hourOfDay = preferences.eveningReminderHour,
                minute = preferences.eveningReminderMinute,
                message = "Evening glucose check"
            )
        }
    }

    fun cancelAllReminders() {
        ReminderWorker.cancelAllReminders(context)
    }
}
