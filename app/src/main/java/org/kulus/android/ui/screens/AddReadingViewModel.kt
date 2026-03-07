package org.kulus.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.kulus.android.data.model.GlucoseUnit
import org.kulus.android.data.api.v3.GlucoseUnits
import org.kulus.android.data.preferences.PreferencesRepository
import org.kulus.android.data.repository.KulusV3Repository
import org.kulus.android.service.GlucoseAlertService
import org.kulus.android.service.NotificationService
import javax.inject.Inject

@HiltViewModel
class AddReadingViewModel @Inject constructor(
    private val repository: KulusV3Repository,
    private val preferencesRepository: PreferencesRepository,
    private val notificationService: NotificationService,
    private val glucoseAlertService: GlucoseAlertService
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddReadingUiState>(AddReadingUiState.Idle)
    val uiState: StateFlow<AddReadingUiState> = _uiState.asStateFlow()

    // Expose user preferences for auto-populating name
    val userPreferences = preferencesRepository.userPreferencesFlow

    fun addReading(
        reading: Double,
        name: String, // Note: v3 uses phone number internally, name is for display only
        units: GlucoseUnit,
        comment: String?,
        snackPass: Boolean,
        photoUri: String? = null,
        source: String = if (photoUri != null) "photo" else "manual",
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            _uiState.value = AddReadingUiState.Loading

            // Convert GlucoseUnit to GlucoseUnits (v3)
            val v3Units = when (units) {
                GlucoseUnit.MMOL_L -> GlucoseUnits.MMOL_L
                GlucoseUnit.MG_DL -> GlucoseUnits.MG_DL
            }

            repository.addReading(
                reading = reading,
                units = v3Units,
                comment = comment.takeIf { !it.isNullOrBlank() },
                snackPass = snackPass,
                photoUri = photoUri,
                source = source,
                tags = tags
            )
                .onSuccess { glucoseReading ->
                    // Check if we should notify about critical levels
                    val preferences = preferencesRepository.userPreferencesFlow.first()
                    notificationService.checkAndNotifyGlucoseLevel(
                        reading = glucoseReading,
                        alertsEnabled = preferences.localAlertsEnabled
                    )
                    // Check advanced alerts (rapid rise/fall, threshold trends)
                    glucoseAlertService.checkAlerts(glucoseReading, preferences)
                    _uiState.value = AddReadingUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = AddReadingUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun resetState() {
        _uiState.value = AddReadingUiState.Idle
    }
}

sealed interface AddReadingUiState {
    object Idle : AddReadingUiState
    object Loading : AddReadingUiState
    object Success : AddReadingUiState
    data class Error(val message: String) : AddReadingUiState
}
