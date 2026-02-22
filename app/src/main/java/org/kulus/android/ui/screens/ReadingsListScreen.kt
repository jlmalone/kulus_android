package org.kulus.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.ui.theme.*
import org.kulus.android.util.TimeRange
import java.text.SimpleDateFormat
import java.util.*

/**
 * ReadingsListScreen - iOS-style History view
 *
 * Matches iOS HistoryView with:
 * - Period selector chips
 * - Statistics section with 2x2 grid
 * - Time in range bars
 * - Filter section
 * - Reading list with better rows
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingsListScreen(
    modifier: Modifier = Modifier,
    viewModel: ReadingsViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {},
    onReadingClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    hideTopBar: Boolean = false,
    hideFab: Boolean = false
) {
    val readings by viewModel.readings.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val syncSuccessMessage by viewModel.syncSuccessMessage.collectAsState()
    val availableTags by viewModel.availableTags.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()

    var selectedPeriod by remember { mutableStateOf(TimeRange.DAYS_7) }

    // Filter readings by selected period
    val filteredReadings = remember(readings, selectedPeriod) {
        val cutoff = System.currentTimeMillis() - selectedPeriod.milliseconds
        readings.filter { it.timestamp >= cutoff }
    }

    // Calculate statistics
    val statistics = remember(filteredReadings) {
        if (filteredReadings.isEmpty()) null
        else calculateStatistics(filteredReadings)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Show success message
    LaunchedEffect(syncSuccessMessage) {
        syncSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }

    // Show error message
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = "Sync failed: $message",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = { Text("History") },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        floatingActionButton = {
            if (!hideFab) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add reading")
                }
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.syncFromServer() },
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period Selector
                item {
                    PeriodSelector(
                        selectedPeriod = selectedPeriod,
                        onPeriodSelected = { selectedPeriod = it }
                    )
                }

                // Filters Section
                if (availableTags.isNotEmpty()) {
                    item {
                        FiltersCard(
                            availableTags = availableTags,
                            selectedTags = selectedTags,
                            onTagToggle = { viewModel.toggleTagFilter(it) },
                            onClearFilters = { viewModel.clearTagFilters() }
                        )
                    }
                }

                // Statistics Section
                if (statistics != null) {
                    item {
                        StatisticsCard(
                            statistics = statistics,
                            periodName = selectedPeriod.displayName
                        )
                    }
                }

                // Readings List Header
                item {
                    ReadingsListHeader(count = filteredReadings.size)
                }

                // Reading Rows
                if (filteredReadings.isEmpty()) {
                    item {
                        EmptyReadingsCard()
                    }
                } else {
                    items(filteredReadings, key = { it.id }) { reading ->
                        ReadingRow(
                            reading = reading,
                            onClick = { onReadingClick(reading.id) }
                        )
                    }
                }

                // Bottom spacer
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: TimeRange,
    onPeriodSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.values().forEach { period ->
            val isSelected = period == selectedPeriod
            Surface(
                onClick = { onPeriodSelected(period) },
                shape = RoundedCornerShape(50),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = period.displayName,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun FiltersCard(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (selectedTags.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "(${selectedTags.size} active)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = onClearFilters) {
                            Text("Clear All", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableTags) { tag ->
                    val isSelected = tag in selectedTags
                    Surface(
                        onClick = { onTagToggle(tag) },
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsCard(
    statistics: ReadingStatistics,
    periodName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Statistics for $periodName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats 2x2 Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = "Average",
                    value = String.format("%.1f mmol/L", statistics.average),
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = "Readings",
                    value = statistics.count.toString(),
                    color = BrandGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = "Min",
                    value = String.format("%.1f mmol/L", statistics.min),
                    color = GlucoseElevated
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    title = "Max",
                    value = String.format("%.1f mmol/L", statistics.max),
                    color = GlucoseHigh
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time in Range
            TimeInRangeSection(statistics.timeInRange)
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun TimeInRangeSection(timeInRange: TimeInRange) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Time in Range",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            TimeInRangeBar(
                label = "Normal (3.9-7.8 mmol/L)",
                percentage = timeInRange.normal,
                color = GlucoseNormal
            )
            TimeInRangeBar(
                label = "Elevated (7.9-11.1 mmol/L)",
                percentage = timeInRange.elevated,
                color = GlucoseElevated
            )
            TimeInRangeBar(
                label = "High (>11.1 mmol/L)",
                percentage = timeInRange.high,
                color = GlucoseHigh
            )
            TimeInRangeBar(
                label = "Low (<3.9 mmol/L)",
                percentage = timeInRange.low,
                color = GlucoseLow
            )
        }
    }
}

@Composable
private fun TimeInRangeBar(
    label: String,
    percentage: Double,
    color: Color
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (percentage / 100).toFloat().coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
private fun ReadingsListHeader(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Readings ($count)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingRow(
    reading: GlucoseReading,
    onClick: () -> Unit
) {
    val levelColor = getLevelColor(reading.color)
    val levelIcon = getLevelIcon(reading.color)
    val levelDescription = getLevelDescription(reading.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level icon
            Icon(
                imageVector = levelIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = levelColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Reading info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format("%.1f %s", reading.reading, reading.units),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatReadingTime(reading.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getSourceIcon(reading.source),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reading.source ?: "Manual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = levelIcon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = levelColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = levelDescription,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = levelColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyReadingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No readings match your filters",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper functions

private fun getLevelColor(color: String?): Color {
    return when (color?.lowercase()) {
        "green" -> GlucoseNormal
        "orange" -> GlucoseElevated
        "red" -> GlucoseHigh
        "purple" -> GlucoseLow
        else -> Color.Gray
    }
}

private fun getLevelDescription(color: String?): String {
    return when (color?.lowercase()) {
        "green" -> "Normal"
        "orange" -> "Elevated"
        "red" -> "High"
        "purple" -> "Low"
        else -> "Unknown"
    }
}

private fun getLevelIcon(color: String?): ImageVector {
    return when (color?.lowercase()) {
        "green" -> Icons.Default.CheckCircle
        "orange" -> Icons.Default.Warning
        "red" -> Icons.Default.Error
        "purple" -> Icons.Default.ArrowDownward
        else -> Icons.Default.HelpOutline
    }
}

private fun getSourceIcon(source: String?): ImageVector {
    return when (source?.lowercase()) {
        "bluetooth" -> Icons.Default.Bluetooth
        "photo", "camera" -> Icons.Default.CameraAlt
        "manual" -> Icons.Default.Edit
        else -> Icons.Default.Input
    }
}

private fun formatReadingTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

// Statistics data classes

data class ReadingStatistics(
    val count: Int,
    val average: Double,
    val min: Double,
    val max: Double,
    val timeInRange: TimeInRange
)

data class TimeInRange(
    val low: Double,
    val normal: Double,
    val elevated: Double,
    val high: Double
)

private fun calculateStatistics(readings: List<GlucoseReading>): ReadingStatistics {
    val values = readings.map { it.reading }
    val count = values.size
    val average = values.average()
    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 0.0

    // Calculate time in range
    val lowCount = readings.count { it.reading < 3.9 }
    val normalCount = readings.count { it.reading in 3.9..7.8 }
    val elevatedCount = readings.count { it.reading in 7.8..11.1 }
    val highCount = readings.count { it.reading > 11.1 }

    val total = count.toDouble().coerceAtLeast(1.0)
    val timeInRange = TimeInRange(
        low = (lowCount / total) * 100,
        normal = (normalCount / total) * 100,
        elevated = (elevatedCount / total) * 100,
        high = (highCount / total) * 100
    )

    return ReadingStatistics(
        count = count,
        average = average,
        min = min,
        max = max,
        timeInRange = timeInRange
    )
}
