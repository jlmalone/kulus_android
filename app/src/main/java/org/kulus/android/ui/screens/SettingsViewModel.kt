package org.kulus.android.ui.screens

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kulus.android.BuildConfig
import org.kulus.android.data.local.TokenStore
import org.kulus.android.data.model.GlucoseUnit
import org.kulus.android.data.preferences.PreferencesRepository
import org.kulus.android.data.preferences.ThemeMode
import org.kulus.android.data.preferences.UserPreferences
import org.kulus.android.data.repository.KulusRepository
import org.kulus.android.util.DataExportService
import org.kulus.android.util.GlucoseStatistics
import java.io.File
import javax.inject.Inject

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val preferences: UserPreferences) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val kulusRepository: KulusRepository,
    private val tokenStore: TokenStore,
    private val dataExportService: DataExportService
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState.asStateFlow()

    // Expose user preferences for direct access (e.g., for setup dialog)
    val userPreferences = preferencesRepository.userPreferencesFlow

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow
                .catch { exception ->
                    _uiState.value = SettingsUiState.Error(
                        exception.message ?: "Failed to load preferences"
                    )
                }
                .collect { preferences ->
                    _uiState.value = SettingsUiState.Success(preferences)
                }
        }
    }

    fun updateDefaultName(name: String) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateDefaultName(name)
            } catch (e: Exception) {
                _actionState.value = ActionState.Error("Failed to update name: ${e.message}")
            }
        }
    }

    fun updatePreferredUnit(unit: GlucoseUnit) {
        viewModelScope.launch {
            try {
                preferencesRepository.updatePreferredUnit(unit)
            } catch (e: Exception) {
                _actionState.value = ActionState.Error("Failed to update unit: ${e.message}")
            }
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateThemeMode(mode)
            } catch (e: Exception) {
                _actionState.value = ActionState.Error("Failed to update theme: ${e.message}")
            }
        }
    }

    fun updateOpenAiApiKey(apiKey: String?) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateOpenAiApiKey(apiKey)
                _actionState.value = ActionState.Success("API key updated")
            } catch (e: Exception) {
                _actionState.value = ActionState.Error("Failed to update API key: ${e.message}")
            }
        }
    }

    fun updateLocalAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateLocalAlertsEnabled(enabled)
            } catch (e: Exception) {
                _actionState.value = ActionState.Error("Failed to update alerts: ${e.message}")
            }
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading("Clearing data...")
            try {
                // Clear readings
                kulusRepository.clearAllReadings()
                    .onSuccess {
                        _actionState.value = ActionState.Success("Local data cleared")
                    }
                    .onFailure { error ->
                        _actionState.value = ActionState.Error(
                            "Failed to clear data: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _actionState.value = ActionState.Error("Failed to clear data: ${e.message}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading("Signing out...")
            try {
                // Clear token
                tokenStore.clearToken()
                // Clear preferences
                preferencesRepository.clearAllPreferences()
                // Clear readings
                kulusRepository.clearAllReadings()

                _actionState.value = ActionState.SignedOut
            } catch (e: Exception) {
                _actionState.value = ActionState.Error("Failed to sign out: ${e.message}")
            }
        }
    }

    fun exportData(format: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading("Preparing export...")
            try {
                // Get current user's readings only (data segregation)
                val readings = kulusRepository.getCurrentUserReadings().first()

                if (readings.isEmpty()) {
                    _actionState.value = ActionState.Error("No readings to export")
                    return@launch
                }

                // Export based on format
                val result = when (format.lowercase()) {
                    "csv" -> dataExportService.exportToCSV(readings)
                    "json" -> dataExportService.exportToJSON(readings)
                    "text" -> {
                        val statistics = GlucoseStatistics.calculate(readings)
                        dataExportService.exportToText(readings, statistics)
                    }
                    else -> Result.failure(IllegalArgumentException("Unknown format: $format"))
                }

                result.onSuccess { file ->
                    val mimeType = when (format.lowercase()) {
                        "csv" -> "text/csv"
                        "json" -> "application/json"
                        "text" -> "text/plain"
                        else -> "application/octet-stream"
                    }

                    val shareIntent = dataExportService.shareFile(file, mimeType)
                    _actionState.value = ActionState.ExportReady(shareIntent, readings.size)
                }.onFailure { error ->
                    _actionState.value = ActionState.Error("Export failed: ${error.message}")
                }

                // Cleanup old exports
                dataExportService.cleanupOldExports()
            } catch (e: Exception) {
                _actionState.value = ActionState.Error("Export failed: ${e.message}")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ActionState.Idle
    }

    fun getAppVersion(): String {
        return "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }
}

sealed class ActionState {
    object Idle : ActionState()
    data class Loading(val message: String) : ActionState()
    data class Success(val message: String) : ActionState()
    data class Error(val message: String) : ActionState()
    object SignedOut : ActionState()
    data class ExportReady(val shareIntent: Intent, val readingCount: Int) : ActionState()
}
