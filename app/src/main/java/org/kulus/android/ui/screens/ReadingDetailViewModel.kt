package org.kulus.android.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.data.repository.KulusRepository
import javax.inject.Inject

sealed class ReadingDetailUiState {
    object Loading : ReadingDetailUiState()
    data class Success(val reading: GlucoseReading) : ReadingDetailUiState()
    data class Error(val message: String) : ReadingDetailUiState()
}

@HiltViewModel
class ReadingDetailViewModel @Inject constructor(
    private val repository: KulusRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val readingId: String = checkNotNull(savedStateHandle["readingId"])

    private val _uiState = MutableStateFlow<ReadingDetailUiState>(ReadingDetailUiState.Loading)
    val uiState: StateFlow<ReadingDetailUiState> = _uiState.asStateFlow()

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState.asStateFlow()

    init {
        loadReading()
    }

    private fun loadReading() {
        viewModelScope.launch {
            _uiState.value = ReadingDetailUiState.Loading
            try {
                val reading = repository.getReadingById(readingId)
                if (reading != null) {
                    _uiState.value = ReadingDetailUiState.Success(reading)
                } else {
                    _uiState.value = ReadingDetailUiState.Error("Reading not found")
                }
            } catch (e: Exception) {
                _uiState.value = ReadingDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteReading() {
        val currentState = _uiState.value
        if (currentState !is ReadingDetailUiState.Success) return

        viewModelScope.launch {
            _deleteState.value = DeleteState.Deleting
            repository.deleteReading(currentState.reading)
                .onSuccess {
                    _deleteState.value = DeleteState.Success
                }
                .onFailure { error ->
                    _deleteState.value = DeleteState.Error(error.message ?: "Failed to delete")
                }
        }
    }

    fun resetDeleteState() {
        _deleteState.value = DeleteState.Idle
    }
}

sealed class DeleteState {
    object Idle : DeleteState()
    object Deleting : DeleteState()
    object Success : DeleteState()
    data class Error(val message: String) : DeleteState()
}
