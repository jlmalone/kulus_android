package org.kulus.android.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kulus.android.data.preferences.DeviceType
import org.kulus.android.data.preferences.PreferencesRepository
import javax.inject.Inject

data class OnboardingState(
    val profileName: String = "",
    val phoneNumber: String = "",
    val selectedDevice: DeviceType = DeviceType.CONTOUR_NEXT_ONE,
    val smsAlertsEnabled: Boolean = true,
    val isCompleting: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun updateProfileName(name: String) {
        _state.value = _state.value.copy(profileName = name, errorMessage = null)
    }

    fun updatePhoneNumber(phone: String) {
        _state.value = _state.value.copy(phoneNumber = phone, errorMessage = null)
    }

    fun updateDeviceType(deviceType: DeviceType) {
        _state.value = _state.value.copy(selectedDevice = deviceType)
    }

    fun updateSmsAlerts(enabled: Boolean) {
        _state.value = _state.value.copy(smsAlertsEnabled = enabled)
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        val currentState = _state.value

        // Validation
        if (currentState.profileName.isBlank()) {
            _state.value = currentState.copy(errorMessage = "Please enter your name")
            return
        }

        _state.value = currentState.copy(isCompleting = true, errorMessage = null)

        viewModelScope.launch {
            try {
                preferencesRepository.completeOnboarding(
                    name = currentState.profileName.trim(),
                    phoneNumber = currentState.phoneNumber.takeIf { it.isNotBlank() },
                    deviceType = currentState.selectedDevice,
                    smsAlertsEnabled = currentState.smsAlertsEnabled
                )
                onComplete()
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isCompleting = false,
                    errorMessage = "Failed to save preferences: ${e.message}"
                )
            }
        }
    }
}
