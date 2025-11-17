package org.kulus.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.kulus.android.ui.components.GlucoseChart
import org.kulus.android.util.TimeRange
import java.util.Locale

/**
 * TrendsScreen - Glucose trends and analytics
 *
 * Features:
 * - Line chart showing glucose over time
 * - Time range selector (24h, 7d, 30d, 90d, 1y)
 * - Statistics cards (avg, min, max, std dev, CV)
 * - Time in range analysis
 * - A1C estimate
 */
@Composable
fun TrendsScreen(
    modifier: Modifier = Modifier,
    viewModel: TrendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (uiState.filteredReadings.isEmpty()) {
            EmptyState(modifier = Modifier.fillMaxWidth())
        } else {
            // Time range selector
            TimeRangeSelector(
                selectedRange = uiState.selectedTimeRange,
                onRangeSelected = { viewModel.selectTimeRange(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Glucose chart
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Glucose Trend",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GlucoseChart(readings = uiState.filteredReadings)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics cards
            uiState.statistics?.let { stats ->
                StatisticsSection(stats = stats)

                Spacer(modifier = Modifier.height(16.dp))

                TimeInRangeSection(stats = stats)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Time Range",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeRange.values().forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { onRangeSelected(range) },
                    label = { Text(range.displayName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatisticsSection(stats: org.kulus.android.util.GlucoseStatistics) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Statistics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Average",
                value = String.format(Locale.US, "%.1f", stats.average),
                unit = "mmol/L",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Min",
                value = String.format(Locale.US, "%.1f", stats.minimum),
                unit = "mmol/L",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Max",
                value = String.format(Locale.US, "%.1f", stats.maximum),
                unit = "mmol/L",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Std Dev",
                value = String.format(Locale.US, "%.1f", stats.standardDeviation),
                unit = "mmol/L",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "CV",
                value = String.format(Locale.US, "%.1f", stats.coefficientOfVariation),
                unit = "%",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Est. A1C",
                value = String.format(Locale.US, "%.1f", stats.estimatedA1C),
                unit = "%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun TimeInRangeSection(stats: org.kulus.android.util.GlucoseStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Time in Range",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // In Range (primary)
            TimeInRangeBar(
                label = "In Range (3.9-10.0 mmol/L)",
                percentage = stats.timeInRange.inRangePercent,
                count = stats.timeInRange.inRangeCount,
                color = org.kulus.android.ui.theme.GlucoseGreen
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Low
            TimeInRangeBar(
                label = "Low (<3.9 mmol/L)",
                percentage = stats.timeInRange.lowPercent + stats.timeInRange.veryLowPercent,
                count = stats.timeInRange.lowCount + stats.timeInRange.veryLowCount,
                color = org.kulus.android.ui.theme.GlucoseOrange
            )

            Spacer(modifier = Modifier.height(12.dp))

            // High
            TimeInRangeBar(
                label = "High (>10.0 mmol/L)",
                percentage = stats.timeInRange.highPercent + stats.timeInRange.veryHighPercent,
                count = stats.timeInRange.highCount + stats.timeInRange.veryHighCount,
                color = org.kulus.android.ui.theme.GlucoseRed
            )
        }
    }
}

@Composable
private fun TimeInRangeBar(
    label: String,
    percentage: Double,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${String.format(Locale.US, "%.1f", percentage)}% ($count)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (percentage / 100).toFloat().coerceIn(0f, 1f))
                    .background(
                        color = color,
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Data Available",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add some glucose readings to see your trends and statistics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
