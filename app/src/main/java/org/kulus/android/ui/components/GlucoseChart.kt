package org.kulus.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.ui.theme.GlucoseGreen
import org.kulus.android.ui.theme.GlucoseOrange
import org.kulus.android.ui.theme.GlucoseRed
import java.text.SimpleDateFormat
import java.util.*

/**
 * GlucoseChart - Line chart for displaying glucose trends over time
 *
 * Features:
 * - Vico library-based chart
 * - Color-coded zones (low/normal/high)
 * - Time-based x-axis
 * - Glucose value y-axis
 * - Smooth line interpolation
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

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Convert readings to chart entries
    val chartEntryModelProducer = remember(readings) {
        val entries = readings.mapIndexed { index, reading ->
            val value = when (reading.units) {
                "mg/dL" -> reading.reading / 18.0 // Convert to mmol/L for display
                else -> reading.reading
            }
            FloatEntry(
                x = index.toFloat(),
                y = value.toFloat()
            )
        }
        ChartEntryModelProducer(entries)
    }

    // Date formatter for x-axis
    val dateFormatter = remember {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            val index = value.toInt()
            if (index in readings.indices) {
                val timestamp = readings[index].timestamp
                val date = Date(timestamp)
                when {
                    readings.size <= 24 -> {
                        // Show hours for 24h view
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                    }
                    readings.size <= 90 -> {
                        // Show dates for week/month view
                        SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
                    }
                    else -> {
                        // Show month for longer periods
                        SimpleDateFormat("MMM yy", Locale.getDefault()).format(date)
                    }
                }
            } else {
                ""
            }
        }
    }

    // Value formatter for y-axis (mmol/L)
    val valueFormatter = remember {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            String.format("%.1f", value)
        }
    }

    Column(modifier = modifier) {
        ProvideChartStyle {
            Chart(
                chart = lineChart(
                    lines = listOf(
                        lineSpec(
                            lineColor = primaryColor.hashCode(),
                            lineBackgroundShader = null
                        )
                    ),
                    targetVerticalAxisPosition = AxisPosition.Vertical.Start
                ),
                chartModelProducer = chartEntryModelProducer,
                startAxis = rememberStartAxis(
                    valueFormatter = valueFormatter,
                    guideline = null
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = dateFormatter,
                    guideline = null
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
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
