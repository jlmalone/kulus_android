package org.kulus.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kulus.android.BuildConfig
import org.kulus.android.data.api.v3.KulusV3ApiService
import org.kulus.android.service.ApiLogEntry
import org.kulus.android.service.ApiLogger
import javax.inject.Inject

@HiltViewModel
class ApiLogViewModel @Inject constructor(
    private val apiLogger: ApiLogger,
    private val apiService: KulusV3ApiService
) : ViewModel() {

    private val _entries = MutableStateFlow<List<ApiLogEntry>>(emptyList())
    val entries: StateFlow<List<ApiLogEntry>> = _entries.asStateFlow()

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    private val _isTesting = MutableStateFlow(false)
    val isTesting: StateFlow<Boolean> = _isTesting.asStateFlow()

    init {
        refreshEntries()
    }

    fun refreshEntries() {
        _entries.value = apiLogger.logEntries
    }

    fun clearLog() {
        apiLogger.clear()
        _entries.value = emptyList()
    }

    fun exportLog(): String {
        return apiLogger.exportAsText()
    }

    fun testApiConnection() {
        viewModelScope.launch {
            _isTesting.value = true
            _testResult.value = null
            try {
                // Use a fake phone number for connectivity test (matches iOS behavior)
                val response = apiService.getReadings(userId = "+10000000000", limit = 1)
                _testResult.value = if (response.isSuccessful) {
                    "Connected (HTTP ${response.code()})"
                } else {
                    "Error: HTTP ${response.code()} — ${response.message()}"
                }
            } catch (e: Exception) {
                _testResult.value = "Failed: ${e.message}"
            } finally {
                _isTesting.value = false
                refreshEntries()
            }
        }
    }

    fun getApiConfig(): Map<String, String> {
        return mapOf(
            "Base URL" to BuildConfig.API_V3_BASE_URL,
            "Partner ID" to BuildConfig.API_V3_PARTNER_ID,
            "API Key" to (BuildConfig.API_V3_KEY.take(10) + "...")
        )
    }
}

/**
 * API Log view matching iOS APILogView.swift.
 * Shows configuration, test button, and full request/response log.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiLogScreen(
    onBackClick: () -> Unit = {},
    viewModel: ApiLogViewModel = hiltViewModel()
) {
    val entries by viewModel.entries.collectAsState()
    val testResult by viewModel.testResult.collectAsState()
    val isTesting by viewModel.isTesting.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Log") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearLog() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear")
                    }
                    IconButton(onClick = { viewModel.refreshEntries() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // API Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "API Configuration",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        viewModel.getApiConfig().forEach { (key, value) ->
                            Row {
                                Text(
                                    "$key: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    value,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Test Connection Button
            item {
                Button(
                    onClick = { viewModel.testApiConnection() },
                    enabled = !isTesting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.NetworkCheck, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test API Connection")
                }

                testResult?.let { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.startsWith("Connected"))
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            result,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Log Entries
            item {
                Text(
                    "Log (${entries.size} entries)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (entries.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No API calls logged yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(entries) { entry ->
                ApiLogEntryCard(entry)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ApiLogEntryCard(entry: ApiLogEntry) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isSuccess)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    entry.formattedTimestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    "${entry.statusCode ?: "ERR"} (${entry.durationMs}ms)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isSuccess) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "${entry.method} ${entry.url.substringAfter("/api/")}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                fontSize = 11.sp
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                entry.requestBody?.let { body ->
                    Text("Request:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(
                        body.take(500),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                entry.responseBody?.let { body ->
                    Text("Response:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(
                        body.take(500),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }

                entry.errorDescription?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Error: $error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
