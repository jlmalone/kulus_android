package org.kulus.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.kulus.android.ui.components.GlucoseReadingCard
import java.text.SimpleDateFormat
import java.util.*

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
                    title = { Text("Kulus") },
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
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        floatingActionButton = {
            if (!hideFab) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Sync status indicator
                SyncStatusBar(
                    lastSyncTime = lastSyncTime,
                    unsyncedCount = readings.count { !it.synced },
                    onSyncClick = { viewModel.syncFromServer() }
                )

                // Tag filters (only show if there are tags)
                if (availableTags.isNotEmpty()) {
                    TagFilterBar(
                        availableTags = availableTags,
                        selectedTags = selectedTags,
                        onTagToggle = { tag -> viewModel.toggleTagFilter(tag) },
                        onClearFilters = { viewModel.clearTagFilters() }
                    )
                }

                if (readings.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No readings yet",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to add your first reading",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(readings, key = { it.id }) { reading ->
                            GlucoseReadingCard(
                                reading = reading,
                                onClick = { onReadingClick(reading.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusBar(
    lastSyncTime: Long?,
    unsyncedCount: Int,
    onSyncClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (unsyncedCount > 0) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (unsyncedCount > 0) Icons.Default.CloudOff else Icons.Default.CloudDone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (unsyncedCount > 0) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (lastSyncTime != null) {
                            "Last sync: ${formatSyncTime(lastSyncTime)}"
                        } else {
                            "Not synced yet"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (unsyncedCount > 0) {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    if (unsyncedCount > 0) {
                        Text(
                            text = "$unsyncedCount reading${if (unsyncedCount > 1) "s" else ""} pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            IconButton(onClick = onSyncClick) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync now",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagFilterBar(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filter by Tags",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (selectedTags.isNotEmpty()) {
                    TextButton(onClick = onClearFilters) {
                        Text("Clear", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableTags) { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = { onTagToggle(tag) },
                        label = { Text(tag) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            if (selectedTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Showing ${selectedTags.size} tag${if (selectedTags.size > 1) "s" else ""}: ${selectedTags.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatSyncTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val formatter = SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
}
