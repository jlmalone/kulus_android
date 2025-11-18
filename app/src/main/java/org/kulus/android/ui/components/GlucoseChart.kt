package org.kulus.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.ui.theme.GlucoseGreen
import org.kulus.android.ui.theme.GlucoseOrange
import org.kulus.android.ui.theme.GlucoseRed

/**
 * GlucoseChart - Line chart for displaying glucose trends over time
 *
 * Features:
 * - Vico library-based chart
 * - Color-coded zones (low/normal/high)
 * - Time-based x-axis
 * - Glucose value y-axis
 * - Smooth line interpolation
 *
 * TODO: Fix Vico 1.13.1 API compatibility
 * - The Vico API has changed significantly in 1.13.1
 * - Need to migrate from old API to new Cartesian API
 * - Requires: CartesianChartHost, rememberCartesianChart, etc.
 */
@Composable
fun GlucoseChart(
    readings: List<GlucoseReading>,
    modifier: Modifier = Modifier
) {
    if (readings.isEmpty()) {
        EmptyChartState(modifier)
        return
    }

    // TODO: Implement Vico 1.13.1 chart
    // The old API (Chart, lineChart, lineSpec) is deprecated
    // Need to use new Cartesian API:
    // - CartesianChartHost
    // - rememberCartesianChart
    // - rememberLineCartesianLayer
    // - CartesianChartModelProducer

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Chart Temporarily Disabled",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vico 1.13.1 API migration needed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${readings.size} readings available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend for glucose ranges
        GlucoseRangeLegend()
    }
}

@Composable
private fun GlucoseRangeLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(color = GlucoseGreen, label = "Normal\n3.9-10.0")
        LegendItem(color = GlucoseOrange, label = "Low\n<3.9")
        LegendItem(color = GlucoseRed, label = "High\n>10.0")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .padding(2.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = color)
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyChartState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No data for selected time range",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
