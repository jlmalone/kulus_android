package org.kulus.android.service

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import org.kulus.android.data.preferences.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OCRService - Extracts glucose values from images using ML Kit Text Recognition
 *
 * This service now supports an enhanced OCR flow:
 * - PRIMARY: OpenAI Vision API (GPT-4o-mini) via OpenAiOcrService
 * - FALLBACK: ML Kit Text Recognition (on-device, no API key needed)
 *
 * If the OpenAI API key is not configured, it skips straight to ML Kit.
 *
 * Patterns detected (ML Kit fallback):
 * - "123 mg/dL" or "123mg/dL"
 * - "6.5 mmol/L" or "6.5mmol/L"
 * - "Blood Glucose: 123"
 * - "BG: 6.5"
 * - Standalone numbers in valid ranges
 */
@Singleton
class OCRService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openAiOcrService: OpenAiOcrService,
    private val preferencesRepository: PreferencesRepository
) {

    companion object {
        private const val TAG = "OCRService"
    }

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
     * Enhanced OCR extraction: tries OpenAI Vision first, falls back to ML Kit.
     *
     * - If openAiApiKey is blank/null, skips straight to ML Kit.
     * - If OpenAI returns an error, falls back to ML Kit.
     * - Returns a unified OCRResult from whichever source succeeds.
     *
     * @param imageUri URI of the image to process
     * @return OCRResult with extracted value or error
     */
    suspend fun extractGlucoseValueEnhanced(imageUri: Uri): OCRResult {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        val apiKey = prefs.openAiApiKey

        // Try OpenAI Vision first if API key is configured
        if (!apiKey.isNullOrBlank()) {
            Log.d(TAG, "Attempting OpenAI Vision OCR (primary)")
            val openAiResult = openAiOcrService.extractGlucoseValue(imageUri, apiKey)

            if (openAiResult is OCRResult.Success) {
                Log.d(TAG, "OpenAI Vision succeeded: ${openAiResult.value} ${openAiResult.unit}")
                return openAiResult
            }

            // Log the failure and fall through to ML Kit
            when (openAiResult) {
                is OCRResult.Error -> Log.w(TAG, "OpenAI Vision failed: ${openAiResult.message}, falling back to ML Kit")
                is OCRResult.NoGlucoseValueFound -> Log.w(TAG, "OpenAI Vision found no glucose value, falling back to ML Kit")
                is OCRResult.NoTextFound -> Log.w(TAG, "OpenAI Vision found no text, falling back to ML Kit")
                else -> Log.w(TAG, "OpenAI Vision returned unexpected result, falling back to ML Kit")
            }
        } else {
            Log.d(TAG, "No OpenAI API key configured, using ML Kit directly")
        }

        // Fallback to ML Kit
        Log.d(TAG, "Attempting ML Kit OCR (fallback)")
        return extractGlucoseValue(imageUri)
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
