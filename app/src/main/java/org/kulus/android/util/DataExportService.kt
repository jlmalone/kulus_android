package org.kulus.android.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.kulus.android.data.model.GlucoseReading
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for exporting glucose readings to various formats
 */
@Singleton
class DataExportService @Inject constructor(
    private val context: Context
) {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    /**
     * Export readings to CSV format
     */
    fun exportToCSV(readings: List<GlucoseReading>): Result<File> {
        return try {
            val file = createExportFile("csv")
            val csvContent = buildString {
                // Header
                appendLine("ID,Reading,Units,Name,Comment,Snack Pass,Source,Timestamp,Date,Synced,Photo URI")

                // Data rows
                readings.forEach { reading ->
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(reading.timestamp))
                    appendLine(
                        "${reading.id}," +
                        "${reading.reading}," +
                        "\"${reading.units}\"," +
                        "\"${reading.name}\"," +
                        "\"${reading.comment?.replace("\"", "\"\"")}\"," +
                        "${reading.snackPass}," +
                        "\"${reading.source}\"," +
                        "${reading.timestamp}," +
                        "\"$date\"," +
                        "${reading.synced}," +
                        "\"${reading.photoUri ?: ""}\""
                    )
                }
            }

            file.writeText(csvContent)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export readings to JSON format
     */
    fun exportToJSON(readings: List<GlucoseReading>): Result<File> {
        return try {
            val file = createExportFile("json")
            val jsonContent = gson.toJson(
                mapOf(
                    "exportDate" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
                    "totalReadings" to readings.size,
                    "readings" to readings.map { reading ->
                        mapOf(
                            "id" to reading.id,
                            "reading" to reading.reading,
                            "units" to reading.units,
                            "name" to reading.name,
                            "comment" to reading.comment,
                            "snackPass" to reading.snackPass,
                            "source" to reading.source,
                            "timestamp" to reading.timestamp,
                            "date" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(reading.timestamp)),
                            "color" to reading.color,
                            "glucoseLevel" to reading.glucoseLevel,
                            "synced" to reading.synced,
                            "photoUri" to reading.photoUri
                        )
                    }
                )
            )

            file.writeText(jsonContent)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a simple text report
     */
    fun exportToText(readings: List<GlucoseReading>, statistics: GlucoseStatistics?): Result<File> {
        return try {
            val file = createExportFile("txt")
            val textContent = buildString {
                appendLine("=".repeat(50))
                appendLine("Kulus Glucose Readings Export")
                appendLine("=".repeat(50))
                appendLine()
                appendLine("Export Date: ${SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.US).format(Date())}")
                appendLine("Total Readings: ${readings.size}")
                appendLine()

                // Statistics (if available)
                statistics?.let { stats ->
                    appendLine("STATISTICS")
                    appendLine("-".repeat(50))
                    appendLine("Average: ${"%.1f".format(stats.average)} mmol/L")
                    appendLine("Minimum: ${"%.1f".format(stats.minimum)} mmol/L")
                    appendLine("Maximum: ${"%.1f".format(stats.maximum)} mmol/L")
                    appendLine("Standard Deviation: ${"%.1f".format(stats.standardDeviation)} mmol/L")
                    appendLine("Coefficient of Variation: ${"%.1f".format(stats.coefficientOfVariation)}%")
                    appendLine("Estimated A1C: ${"%.1f".format(stats.estimatedA1C)}%")
                    appendLine()
                    appendLine("TIME IN RANGE")
                    appendLine("-".repeat(50))
                    appendLine("In Range (3.9-10.0): ${"%.1f".format(stats.timeInRange.inRangePercent)}% (${stats.timeInRange.inRangeCount} readings)")
                    appendLine("Low (<3.9): ${"%.1f".format(stats.timeInRange.lowPercent + stats.timeInRange.veryLowPercent)}% (${stats.timeInRange.lowCount + stats.timeInRange.veryLowCount} readings)")
                    appendLine("High (>10.0): ${"%.1f".format(stats.timeInRange.highPercent + stats.timeInRange.veryHighPercent)}% (${stats.timeInRange.highCount + stats.timeInRange.veryHighCount} readings)")
                    appendLine()
                }

                // Readings
                appendLine("READINGS")
                appendLine("-".repeat(50))
                readings.sortedByDescending { it.timestamp }.forEach { reading ->
                    val date = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US).format(Date(reading.timestamp))
                    appendLine("$date - ${reading.name}")
                    appendLine("  Reading: ${reading.reading} ${reading.units}")
                    reading.comment?.let { appendLine("  Comment: $it") }
                    if (reading.snackPass) appendLine("  [Snack Pass]")
                    if (!reading.synced) appendLine("  [Not Synced]")
                    appendLine()
                }
            }

            file.writeText(textContent)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Share exported file using Android's share intent
     */
    fun shareFile(file: File, mimeType: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Kulus Glucose Readings Export")
            putExtra(Intent.EXTRA_TEXT, "Glucose readings exported from Kulus")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Create a timestamped export file
     */
    private fun createExportFile(extension: String): File {
        val exportDir = File(context.cacheDir, "exports").apply {
            if (!exists()) mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "kulus_export_$timestamp.$extension"

        return File(exportDir, filename)
    }

    /**
     * Clean up old export files (keep only last 5)
     */
    fun cleanupOldExports() {
        val exportDir = File(context.cacheDir, "exports")
        if (exportDir.exists()) {
            exportDir.listFiles()
                ?.sortedByDescending { it.lastModified() }
                ?.drop(5)
                ?.forEach { it.delete() }
        }
    }
}
