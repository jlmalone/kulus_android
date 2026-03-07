package org.kulus.android.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * OpenAiOcrService - Extracts glucose values from images using OpenAI Vision API (GPT-4o-mini)
 *
 * This is the PRIMARY OCR method, matching the iOS Hamumu app's approach.
 * ML Kit OCRService serves as the fallback when no API key is configured or the API fails.
 *
 * Features:
 * - Sends glucose meter photo to OpenAI Vision API
 * - Base64 encodes and compresses images (max ~1MB)
 * - Structured prompt for glucose reading extraction
 * - Parses response to extract value, units, and confidence
 * - Graceful error handling for network, auth, and rate limit errors
 */
@Singleton
class OpenAiOcrService @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("openAiOkHttpClient") private val httpClient: OkHttpClient
) {

    companion object {
        private const val TAG = "OpenAiOcrService"
        private const val OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"
        private const val MODEL = "gpt-4o-mini"
        private const val MAX_IMAGE_DIMENSION = 1024
        private const val JPEG_QUALITY = 80
        private const val MAX_IMAGE_BYTES = 1_000_000 // ~1MB
    }

    /**
     * Extract glucose value from an image using OpenAI Vision API.
     *
     * @param imageUri URI of the image to process
     * @param apiKey OpenAI API key
     * @return OCRResult with extracted value or error
     */
    suspend fun extractGlucoseValue(imageUri: Uri, apiKey: String): OCRResult {
        return withContext(Dispatchers.IO) {
            try {
                val base64Image = encodeImageToBase64(imageUri)
                    ?: return@withContext OCRResult.Error("Failed to read or encode the image")

                val requestBody = buildRequestBody(base64Image)
                val request = Request.Builder()
                    .url(OPENAI_API_URL)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    val errorMessage = parseErrorMessage(response.code, responseBody)
                    Log.w(TAG, "OpenAI API error: ${response.code} - $errorMessage")
                    return@withContext OCRResult.Error(errorMessage)
                }

                if (responseBody == null) {
                    return@withContext OCRResult.Error("Empty response from OpenAI API")
                }

                parseOpenAiResponse(responseBody)

            } catch (e: java.net.UnknownHostException) {
                Log.w(TAG, "Network error: no internet", e)
                OCRResult.Error("No internet connection. Please check your network.")
            } catch (e: java.net.SocketTimeoutException) {
                Log.w(TAG, "Network error: timeout", e)
                OCRResult.Error("Request timed out. Please try again.")
            } catch (e: Exception) {
                Log.e(TAG, "OpenAI OCR failed", e)
                OCRResult.Error("OpenAI Vision failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /**
     * Encode image to base64, resizing and compressing to stay under MAX_IMAGE_BYTES.
     */
    private fun encodeImageToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return null

            // Resize if needed
            val bitmap = resizeBitmap(originalBitmap, MAX_IMAGE_DIMENSION)
            if (bitmap != originalBitmap) {
                originalBitmap.recycle()
            }

            // Compress to JPEG
            var quality = JPEG_QUALITY
            var base64: String
            do {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                val bytes = outputStream.toByteArray()
                base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                quality -= 10
            } while (bytes.size > MAX_IMAGE_BYTES && quality > 20)

            bitmap.recycle()
            base64
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encode image", e)
            null
        }
    }

    /**
     * Resize bitmap so the longest side is at most maxDimension.
     */
    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }

        val scale = maxDimension.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Build the OpenAI chat completions request body with vision content.
     */
    private fun buildRequestBody(base64Image: String): JSONObject {
        val imageContent = JSONObject().apply {
            put("type", "image_url")
            put("image_url", JSONObject().apply {
                put("url", "data:image/jpeg;base64,$base64Image")
                put("detail", "high")
            })
        }

        val textContent = JSONObject().apply {
            put("type", "text")
            put("text", buildString {
                append("Look at this glucose meter display photo. ")
                append("Identify the glucose reading value shown on the meter's screen. ")
                append("Respond ONLY with a JSON object in this exact format (no markdown, no explanation):\n")
                append("{\"value\": <number>, \"units\": \"mg/dL\" or \"mmol/L\", \"confidence\": \"high\" or \"medium\" or \"low\"}\n\n")
                append("Rules:\n")
                append("- value: the numeric glucose reading (e.g., 120 for mg/dL or 6.7 for mmol/L)\n")
                append("- units: \"mg/dL\" if the value is typically 20-600, \"mmol/L\" if typically 1.1-33.3\n")
                append("- confidence: \"high\" if the number is clearly visible, \"medium\" if partially obscured, \"low\" if uncertain\n")
                append("- If you cannot identify any glucose reading, respond with: {\"error\": \"no reading found\"}")
            })
        }

        val userMessage = JSONObject().apply {
            put("role", "user")
            put("content", JSONArray().apply {
                put(textContent)
                put(imageContent)
            })
        }

        return JSONObject().apply {
            put("model", MODEL)
            put("messages", JSONArray().apply { put(userMessage) })
            put("max_tokens", 150)
            put("temperature", 0.1)
        }
    }

    /**
     * Parse the OpenAI API response and extract glucose reading data.
     */
    private fun parseOpenAiResponse(responseBody: String): OCRResult {
        return try {
            val json = JSONObject(responseBody)
            val choices = json.getJSONArray("choices")
            if (choices.length() == 0) {
                return OCRResult.Error("No response from OpenAI Vision")
            }

            val content = choices.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            // Strip markdown code fences if present
            val cleanContent = content
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val resultJson = JSONObject(cleanContent)

            // Check for error response
            if (resultJson.has("error")) {
                val errorMsg = resultJson.getString("error")
                return OCRResult.NoGlucoseValueFound(errorMsg)
            }

            val value = resultJson.getDouble("value")
            val units = resultJson.getString("units")
            val confidenceStr = resultJson.optString("confidence", "medium")

            val confidence = when (confidenceStr.lowercase()) {
                "high" -> OCRConfidence.HIGH
                "medium" -> OCRConfidence.MEDIUM
                else -> OCRConfidence.LOW
            }

            // Validate the extracted value
            val isValid = when (units) {
                "mg/dL" -> value in 20.0..600.0
                "mmol/L" -> value in 1.1..33.3
                else -> false
            }

            if (!isValid) {
                return OCRResult.Error(
                    "Extracted value $value $units is outside the valid glucose range"
                )
            }

            OCRResult.Success(
                value = value,
                unit = units,
                confidence = confidence,
                rawText = "OpenAI Vision: $cleanContent"
            )
        } catch (e: org.json.JSONException) {
            Log.w(TAG, "Failed to parse OpenAI response as JSON", e)
            OCRResult.Error("Could not parse OpenAI Vision response")
        }
    }

    /**
     * Parse error messages from OpenAI API error responses.
     */
    private fun parseErrorMessage(statusCode: Int, responseBody: String?): String {
        val detail = try {
            responseBody?.let {
                JSONObject(it).getJSONObject("error").getString("message")
            }
        } catch (e: Exception) {
            null
        }

        return when (statusCode) {
            401 -> "Invalid OpenAI API key. Please check your key in Settings."
            429 -> "OpenAI rate limit exceeded. Please try again in a moment."
            500, 502, 503 -> "OpenAI service temporarily unavailable. Please try again."
            else -> detail ?: "OpenAI API error (HTTP $statusCode)"
        }
    }
}
