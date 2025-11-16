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
                targetRangeHigh = preferences[PreferencesKeys.TARGET_RANGE_HIGH] ?: 7.8
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
}
