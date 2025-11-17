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

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    private val allReadings: StateFlow<List<GlucoseReading>> = repository
        .getAllReadingsLocal()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val readings: StateFlow<List<GlucoseReading>> = combine(
        allReadings,
        _selectedTags
    ) { readings, selectedTags ->
        if (selectedTags.isEmpty()) {
            readings
        } else {
            readings.filter { reading ->
                val readingTags = reading.getTagsList()
                selectedTags.any { tag -> readingTags.contains(tag) }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Get all unique tags from all readings
    val availableTags: StateFlow<List<String>> = allReadings.map { readings ->
        readings.flatMap { it.getTagsList() }
            .distinct()
            .sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()

    private val _syncSuccessMessage = MutableStateFlow<String?>(null)
    val syncSuccessMessage: StateFlow<String?> = _syncSuccessMessage.asStateFlow()

    fun syncFromServer() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _syncSuccessMessage.value = null
            repository.syncReadingsFromServer()
                .onSuccess { readings ->
                    _errorMessage.value = null
                    _lastSyncTime.value = System.currentTimeMillis()
                    _syncSuccessMessage.value = "Synced ${readings.size} readings"
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                    _syncSuccessMessage.value = null
                }
            _isRefreshing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _syncSuccessMessage.value = null
    }

    fun toggleTagFilter(tag: String) {
        _selectedTags.value = if (tag in _selectedTags.value) {
            _selectedTags.value - tag
        } else {
            _selectedTags.value + tag
        }
    }

    fun clearTagFilters() {
        _selectedTags.value = emptySet()
    }
}
