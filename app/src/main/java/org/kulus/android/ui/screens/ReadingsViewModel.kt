package org.kulus.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.data.repository.KulusRepository
import javax.inject.Inject

@HiltViewModel
class ReadingsViewModel @Inject constructor(
    private val repository: KulusRepository
) : ViewModel() {

    val readings: StateFlow<List<GlucoseReading>> = repository
        .getAllReadingsLocal()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun syncFromServer() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.syncReadingsFromServer()
                .onSuccess {
                    _errorMessage.value = null
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            _isRefreshing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
