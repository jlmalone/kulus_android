package org.kulus.android.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import org.kulus.android.MainActivity
import org.kulus.android.R
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.data.model.GlucoseUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID_ALERTS = "glucose_alerts"
        private const val CHANNEL_ID_REMINDERS = "glucose_reminders"
        private const val NOTIFICATION_ID_ALERT = 1001
        private const val NOTIFICATION_ID_REMINDER = 1002

        // Glucose thresholds in mmol/L
        private const val CRITICAL_HIGH_MMOL = 13.9  // ~250 mg/dL
        private const val HIGH_MMOL = 10.0           // ~180 mg/dL
        private const val LOW_MMOL = 3.9             // ~70 mg/dL
        private const val CRITICAL_LOW_MMOL = 3.0    // ~54 mg/dL
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Critical alerts channel
            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Glucose Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical glucose level alerts"
                enableVibration(true)
                enableLights(true)
            }

            // Reminders channel
            val remindersChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Testing Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to test glucose levels"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(alertsChannel)
            notificationManager.createNotificationChannel(remindersChannel)
        }
    }

    fun checkAndNotifyGlucoseLevel(
        reading: GlucoseReading,
        alertsEnabled: Boolean,
        customThresholds: Pair<Double, Double>? = null
    ) {
        // Don't notify if alerts disabled or reading has snack pass
        if (!alertsEnabled || reading.snackPass) {
            return
        }

        // Convert to mmol/L for threshold comparison
        val readingInMmol = if (reading.units == GlucoseUnit.MG_DL.apiValue) {
            reading.reading / 18.0
        } else {
            reading.reading
        }

        val severity = determineGlucoseSeverity(readingInMmol, customThresholds)

        when (severity) {
            GlucoseSeverity.CRITICAL_HIGH,
            GlucoseSeverity.CRITICAL_LOW -> {
                showCriticalAlert(reading, severity)
            }
            GlucoseSeverity.HIGH,
            GlucoseSeverity.LOW -> {
                // Optional: show less urgent notifications
                // Can be configurable in future
            }
            GlucoseSeverity.NORMAL -> {
                // No notification needed
            }
        }
    }

    private fun determineGlucoseSeverity(
        readingInMmol: Double,
        customThresholds: Pair<Double, Double>? = null
    ): GlucoseSeverity {
        return when {
            readingInMmol >= CRITICAL_HIGH_MMOL -> GlucoseSeverity.CRITICAL_HIGH
            readingInMmol <= CRITICAL_LOW_MMOL -> GlucoseSeverity.CRITICAL_LOW
            readingInMmol >= HIGH_MMOL -> GlucoseSeverity.HIGH
            readingInMmol <= LOW_MMOL -> GlucoseSeverity.LOW
            else -> GlucoseSeverity.NORMAL
        }
    }

    private fun showCriticalAlert(reading: GlucoseReading, severity: GlucoseSeverity) {
        if (!hasNotificationPermission()) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reading_id", reading.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val (title, message, priority) = when (severity) {
            GlucoseSeverity.CRITICAL_HIGH -> Triple(
                "⚠️ Critical High Glucose",
                "Your glucose is ${reading.reading} ${reading.units}. This is dangerously high. Please take action.",
                NotificationCompat.PRIORITY_MAX
            )
            GlucoseSeverity.CRITICAL_LOW -> Triple(
                "🚨 Critical Low Glucose",
                "Your glucose is ${reading.reading} ${reading.units}. This is dangerously low. Treat immediately.",
                NotificationCompat.PRIORITY_MAX
            )
            else -> return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_ALERT, notification)
    }

    fun showTestingReminder(message: String = "Time to check your glucose level") {
        if (!hasNotificationPermission()) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Glucose Testing Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_REMINDER, notification)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    enum class GlucoseSeverity {
        CRITICAL_LOW,
        LOW,
        NORMAL,
        HIGH,
        CRITICAL_HIGH
    }
}
