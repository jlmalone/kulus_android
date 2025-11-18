package org.kulus.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.data.repository.KulusRepository
import org.kulus.android.util.GlucoseStatistics
import org.kulus.android.util.TimeRange
import org.kulus.android.util.filterByTimeRange
import javax.inject.Inject

data class TrendsUiState(
    val selectedTimeRange: TimeRange = TimeRange.DAYS_7,
    val readings: List<GlucoseReading> = emptyList(),
    val filteredReadings: List<GlucoseReading> = emptyList(),
    val statistics: GlucoseStatistics? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val repository: KulusRepository
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRange.DAYS_7)

    // CRITICAL: Use getCurrentUserReadings() to prevent showing other users' data
    val uiState: StateFlow<TrendsUiState> = combine(
        repository.getCurrentUserReadings(),
        _selectedTimeRange
    ) { readings, timeRange ->
        val filteredReadings = readings
            .filterByTimeRange(timeRange)
            .sortedBy { it.timestamp }

        val statistics = GlucoseStatistics.calculate(filteredReadings)

        TrendsUiState(
            selectedTimeRange = timeRange,
            readings = readings,
            filteredReadings = filteredReadings,
            statistics = statistics,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrendsUiState(isLoading = true)
    )

    fun selectTimeRange(timeRange: TimeRange) {
        _selectedTimeRange.value = timeRange
    }
}
