package org.kulus.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.kulus.android.data.model.GlucoseUnit
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val DEFAULT_NAME = stringPreferencesKey("default_name")
        val PREFERRED_UNIT = stringPreferencesKey("preferred_unit")
        val THEME_MODE = intPreferencesKey("theme_mode")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val TARGET_RANGE_LOW = doublePreferencesKey("target_range_low")
        val TARGET_RANGE_HIGH = doublePreferencesKey("target_range_high")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val SELECTED_DEVICE_TYPE = intPreferencesKey("selected_device_type")
        val SMS_ALERTS_ENABLED = booleanPreferencesKey("sms_alerts_enabled")
        val LOCAL_ALERTS_ENABLED = booleanPreferencesKey("local_alerts_enabled")
        val CRITICAL_LOW_THRESHOLD = doublePreferencesKey("critical_low_threshold")
        val CRITICAL_HIGH_THRESHOLD = doublePreferencesKey("critical_high_threshold")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val MORNING_REMINDER_ENABLED = booleanPreferencesKey("morning_reminder_enabled")
        val MORNING_REMINDER_HOUR = intPreferencesKey("morning_reminder_hour")
        val MORNING_REMINDER_MINUTE = intPreferencesKey("morning_reminder_minute")
        val EVENING_REMINDER_ENABLED = booleanPreferencesKey("evening_reminder_enabled")
        val EVENING_REMINDER_HOUR = intPreferencesKey("evening_reminder_hour")
        val EVENING_REMINDER_MINUTE = intPreferencesKey("evening_reminder_minute")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                defaultName = preferences[PreferencesKeys.DEFAULT_NAME] ?: "mobile-user",
                preferredUnit = GlucoseUnit.fromString(
                    preferences[PreferencesKeys.PREFERRED_UNIT] ?: "mmol/L"
                ),
                themeMode = ThemeMode.fromOrdinal(
                    preferences[PreferencesKeys.THEME_MODE] ?: 0
                ),
                openAiApiKey = preferences[PreferencesKeys.OPENAI_API_KEY],
                targetRangeLow = preferences[PreferencesKeys.TARGET_RANGE_LOW] ?: 3.9,
                targetRangeHigh = preferences[PreferencesKeys.TARGET_RANGE_HIGH] ?: 7.8,
                onboardingCompleted = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
                phoneNumber = preferences[PreferencesKeys.PHONE_NUMBER],
                selectedDeviceType = DeviceType.fromOrdinal(
                    preferences[PreferencesKeys.SELECTED_DEVICE_TYPE] ?: 0
                ),
                smsAlertsEnabled = preferences[PreferencesKeys.SMS_ALERTS_ENABLED] ?: true,
                localAlertsEnabled = preferences[PreferencesKeys.LOCAL_ALERTS_ENABLED] ?: true,
                criticalLowThreshold = preferences[PreferencesKeys.CRITICAL_LOW_THRESHOLD] ?: 3.0,
                criticalHighThreshold = preferences[PreferencesKeys.CRITICAL_HIGH_THRESHOLD] ?: 13.9,
                remindersEnabled = preferences[PreferencesKeys.REMINDERS_ENABLED] ?: false,
                morningReminderEnabled = preferences[PreferencesKeys.MORNING_REMINDER_ENABLED] ?: false,
                morningReminderHour = preferences[PreferencesKeys.MORNING_REMINDER_HOUR] ?: 8,
                morningReminderMinute = preferences[PreferencesKeys.MORNING_REMINDER_MINUTE] ?: 0,
                eveningReminderEnabled = preferences[PreferencesKeys.EVENING_REMINDER_ENABLED] ?: false,
                eveningReminderHour = preferences[PreferencesKeys.EVENING_REMINDER_HOUR] ?: 20,
                eveningReminderMinute = preferences[PreferencesKeys.EVENING_REMINDER_MINUTE] ?: 0
            )
        }

    suspend fun updateDefaultName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_NAME] = name
        }
    }

    suspend fun updatePreferredUnit(unit: GlucoseUnit) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_UNIT] = unit.apiValue
        }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.ordinal
        }
    }

    suspend fun updateOpenAiApiKey(apiKey: String?) {
        context.dataStore.edit { preferences ->
            if (apiKey.isNullOrBlank()) {
                preferences.remove(PreferencesKeys.OPENAI_API_KEY)
            } else {
                preferences[PreferencesKeys.OPENAI_API_KEY] = apiKey
            }
        }
    }

    suspend fun updateTargetRange(low: Double, high: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TARGET_RANGE_LOW] = low
            preferences[PreferencesKeys.TARGET_RANGE_HIGH] = high
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun completeOnboarding(
        name: String,
        phoneNumber: String?,
        deviceType: DeviceType,
        smsAlertsEnabled: Boolean
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = true
            preferences[PreferencesKeys.DEFAULT_NAME] = name
            if (!phoneNumber.isNullOrBlank()) {
                preferences[PreferencesKeys.PHONE_NUMBER] = phoneNumber
            }
            preferences[PreferencesKeys.SELECTED_DEVICE_TYPE] = deviceType.ordinal
            preferences[PreferencesKeys.SMS_ALERTS_ENABLED] = smsAlertsEnabled
        }
    }

    suspend fun updatePhoneNumber(phoneNumber: String?) {
        context.dataStore.edit { preferences ->
            if (phoneNumber.isNullOrBlank()) {
                preferences.remove(PreferencesKeys.PHONE_NUMBER)
            } else {
                preferences[PreferencesKeys.PHONE_NUMBER] = phoneNumber
            }
        }
    }

    suspend fun updateDeviceType(deviceType: DeviceType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_DEVICE_TYPE] = deviceType.ordinal
        }
    }

    suspend fun updateSmsAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SMS_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun updateLocalAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCAL_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun updateCriticalThresholds(low: Double, high: Double) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CRITICAL_LOW_THRESHOLD] = low
            preferences[PreferencesKeys.CRITICAL_HIGH_THRESHOLD] = high
        }
    }

    suspend fun updateRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun updateMorningReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MORNING_REMINDER_ENABLED] = enabled
            preferences[PreferencesKeys.MORNING_REMINDER_HOUR] = hour
            preferences[PreferencesKeys.MORNING_REMINDER_MINUTE] = minute
        }
    }

    suspend fun updateEveningReminder(enabled: Boolean, hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EVENING_REMINDER_ENABLED] = enabled
            preferences[PreferencesKeys.EVENING_REMINDER_HOUR] = hour
            preferences[PreferencesKeys.EVENING_REMINDER_MINUTE] = minute
        }
    }
}
