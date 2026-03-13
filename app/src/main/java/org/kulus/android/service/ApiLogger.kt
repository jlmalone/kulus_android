package org.kulus.android.service

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe API logger that captures all HTTP requests/responses.
 * Matching iOS APILogger feature: full request/response capture visible in Settings > Debug > API Log.
 */
data class ApiLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Date = Date(),
    val method: String,
    val url: String,
    val requestHeaders: Map<String, String>,
    val requestBody: String?,
    val statusCode: Int?,
    val responseBody: String?,
    val errorDescription: String?,
    val durationMs: Long
) {
    val formattedTimestamp: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(timestamp)

    val isSuccess: Boolean
        get() = statusCode != null && statusCode in 200..299

    /** Redact API key for display (show first 10 chars + ...) */
    fun redactedHeaders(): Map<String, String> {
        return requestHeaders.mapValues { (key, value) ->
            if (key.equals("x-api-key", ignoreCase = true) && value.length > 10) {
                value.take(10) + "..."
            } else {
                value
            }
        }
    }
}

@Singleton
class ApiLogger @Inject constructor() {

    private val entries = ConcurrentLinkedDeque<ApiLogEntry>()

    val logEntries: List<ApiLogEntry>
        get() = entries.toList()

    val entryCount: Int
        get() = entries.size

    fun addEntry(entry: ApiLogEntry) {
        entries.addFirst(entry)
        // Keep last 100 entries
        while (entries.size > 100) {
            entries.removeLast()
        }
    }

    fun clear() {
        entries.clear()
    }

    fun exportAsText(): String {
        return buildString {
            appendLine("=== Kulus API Log ===")
            appendLine("Exported: ${Date()}")
            appendLine("Entries: ${entries.size}")
            appendLine()
            entries.forEach { entry ->
                appendLine("--- ${entry.formattedTimestamp} ---")
                appendLine("${entry.method} ${entry.url}")
                appendLine("Status: ${entry.statusCode ?: "N/A"}")
                appendLine("Duration: ${entry.durationMs}ms")
                entry.requestBody?.let { appendLine("Request: $it") }
                entry.responseBody?.let {
                    appendLine("Response: ${it.take(500)}")
                }
                entry.errorDescription?.let { appendLine("Error: $it") }
                appendLine()
            }
        }
    }
}

/**
 * OkHttp interceptor that logs all requests/responses to ApiLogger.
 */
class ApiLoggerInterceptor(private val apiLogger: ApiLogger) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()

        // Capture request
        val method = request.method
        val url = request.url.toString()
        val headers = request.headers.toMultimap().mapValues { it.value.joinToString(", ") }

        val requestBody = request.body?.let { body ->
            try {
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            } catch (e: Exception) {
                "<error reading body>"
            }
        }

        return try {
            val response = chain.proceed(request)
            val durationMs = System.currentTimeMillis() - startTime

            // Read response body (must recreate it since it can only be read once)
            val responseBodyString = response.body?.let { body ->
                try {
                    val source = body.source()
                    source.request(Long.MAX_VALUE)
                    source.buffer.clone().readUtf8()
                } catch (e: Exception) {
                    "<error reading response>"
                }
            }

            val entry = ApiLogEntry(
                method = method,
                url = url,
                requestHeaders = headers,
                requestBody = requestBody,
                statusCode = response.code,
                responseBody = responseBodyString,
                errorDescription = if (!response.isSuccessful) "HTTP ${response.code}: ${response.message}" else null,
                durationMs = durationMs
            )
            apiLogger.addEntry(entry)

            Log.d("ApiLogger", "${entry.formattedTimestamp} ${method} ${url} -> ${response.code} (${durationMs}ms)")

            response
        } catch (e: Exception) {
            val durationMs = System.currentTimeMillis() - startTime
            val entry = ApiLogEntry(
                method = method,
                url = url,
                requestHeaders = headers,
                requestBody = requestBody,
                statusCode = null,
                responseBody = null,
                errorDescription = e.message ?: e.javaClass.simpleName,
                durationMs = durationMs
            )
            apiLogger.addEntry(entry)
            throw e
        }
    }
}
