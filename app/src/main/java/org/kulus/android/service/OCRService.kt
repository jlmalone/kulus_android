package org.kulus.android.service

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OCRService - Extracts glucose values from images using ML Kit Text Recognition
 *
 * Features:
 * - Uses ML Kit Vision API for text recognition
 * - Intelligent glucose value extraction with regex patterns
 * - Automatic unit detection (mg/dL vs mmol/L)
 * - Confidence scoring based on match quality
 * - Supports multiple glucose value formats
 *
 * Patterns detected:
 * - "123 mg/dL" or "123mg/dL"
 * - "6.5 mmol/L" or "6.5mmol/L"
 * - "Blood Glucose: 123"
 * - "BG: 6.5"
 * - Standalone numbers in valid ranges
 */
@Singleton
class OCRService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Extract glucose value from an image
     *
     * @param imageUri URI of the image to process
     * @return OCRResult with extracted value or error
     */
    suspend fun extractGlucoseValue(imageUri: Uri): OCRResult {
        return try {
            val inputImage = InputImage.fromFilePath(context, imageUri)
            val visionText = textRecognizer.process(inputImage).await()

            val fullText = visionText.text
            if (fullText.isBlank()) {
                return OCRResult.NoTextFound
            }

            // Try multiple extraction strategies in order of specificity
            extractWithExplicitUnit(fullText)
                ?: extractWithLabel(fullText)
                ?: extractStandaloneNumber(fullText)
                ?: OCRResult.NoGlucoseValueFound(fullText)

        } catch (e: Exception) {
            OCRResult.Error(e.message ?: "Unknown OCR error")
        }
    }

    /**
     * Extract glucose value with explicit unit (e.g., "123 mg/dL", "6.5 mmol/L")
     * Highest confidence pattern
     */
    private fun extractWithExplicitUnit(text: String): OCRResult.Success? {
        // Pattern: number followed by unit (with optional space)
        val patterns = listOf(
            // mg/dL patterns
            Regex("""(\d+)\s*mg\s*/\s*d[lL]""", RegexOption.IGNORE_CASE),
            Regex("""(\d+)\s*mg\s*dl""", RegexOption.IGNORE_CASE),

            // mmol/L patterns
            Regex("""(\d+\.?\d*)\s*mmol\s*/\s*[lL]""", RegexOption.IGNORE_CASE),
            Regex("""(\d+\.?\d*)\s*mmol\s*l""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val valueStr = match.groupValues[1]
                val value = valueStr.toDoubleOrNull() ?: continue

                val unit = detectUnitFromPattern(match.value)
                if (isValidGlucoseValue(value, unit)) {
                    return OCRResult.Success(
                        value = value,
                        unit = unit,
                        confidence = OCRConfidence.HIGH,
                        rawText = text
                    )
                }
            }
        }

        return null
    }

    /**
     * Extract glucose value with label (e.g., "Blood Glucose: 123", "BG: 6.5")
     * Medium confidence pattern
     */
    private fun extractWithLabel(text: String): OCRResult.Success? {
        val patterns = listOf(
            Regex("""(?:blood\s*glucose|bg|glucose|sugar)[\s:]+(\d+\.?\d*)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val valueStr = match.groupValues[1]
                val value = valueStr.toDoubleOrNull() ?: continue

                val unit = detectUnitFromValue(value)
                if (isValidGlucoseValue(value, unit)) {
                    return OCRResult.Success(
                        value = value,
                        unit = unit,
                        confidence = OCRConfidence.MEDIUM,
                        rawText = text
                    )
                }
            }
        }

        return null
    }

    /**
     * Extract standalone number in valid glucose range
     * Lowest confidence pattern - only use if nothing else matches
     */
    private fun extractStandaloneNumber(text: String): OCRResult.Success? {
        // Look for numbers that could be glucose values
        val numberPattern = Regex("""(?<![.\d])(\d+\.?\d*)(?![.\d])""")
        val matches = numberPattern.findAll(text)

        for (match in matches) {
            val valueStr = match.groupValues[1]
            val value = valueStr.toDoubleOrNull() ?: continue

            // Try both units to see if either is valid
            val mgdlValid = isValidGlucoseValue(value, "mg/dL")
            val mmolValid = isValidGlucoseValue(value, "mmol/L")

            if (mgdlValid || mmolValid) {
                val unit = if (mmolValid) "mmol/L" else "mg/dL"
                return OCRResult.Success(
                    value = value,
                    unit = unit,
                    confidence = OCRConfidence.LOW,
                    rawText = text
                )
            }
        }

        return null
    }

    /**
     * Detect unit from matched pattern text
     */
    private fun detectUnitFromPattern(patternText: String): String {
        return if (patternText.contains("mmol", ignoreCase = true)) {
            "mmol/L"
        } else {
            "mg/dL"
        }
    }

    /**
     * Detect unit from value based on typical ranges
     * mg/dL: 20-600 (typical: 70-200)
     * mmol/L: 1.1-33.3 (typical: 3.9-11.1)
     */
    private fun detectUnitFromValue(value: Double): String {
        return if (value < 35) {
            // Likely mmol/L since mg/dL values are rarely this low
            "mmol/L"
        } else {
            // Likely mg/dL
            "mg/dL"
        }
    }

    /**
     * Validate glucose value is in acceptable range for the given unit
     */
    private fun isValidGlucoseValue(value: Double, unit: String): Boolean {
        return when (unit) {
            "mg/dL" -> value in 20.0..600.0
            "mmol/L" -> value in 1.1..33.3
            else -> false
        }
    }

    /**
     * Clean up resources
     */
    fun close() {
        textRecognizer.close()
    }
}

/**
 * Result of OCR operation
 */
sealed class OCRResult {
    data class Success(
        val value: Double,
        val unit: String,
        val confidence: OCRConfidence,
        val rawText: String
    ) : OCRResult()

    object NoTextFound : OCRResult()

    data class NoGlucoseValueFound(val rawText: String) : OCRResult()

    data class Error(val message: String) : OCRResult()
}

/**
 * Confidence level of OCR extraction
 */
enum class OCRConfidence(val displayName: String) {
    HIGH("High - Value with unit detected"),
    MEDIUM("Medium - Value with label detected"),
    LOW("Low - Standalone number detected")
}
