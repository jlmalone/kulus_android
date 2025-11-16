package org.kulus.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingDetailScreen(
    viewModel: ReadingDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Handle delete success - navigate back
    LaunchedEffect(deleteState) {
        if (deleteState is DeleteState.Success) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reading Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ReadingDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is ReadingDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBackClick) {
                            Text("Go Back")
                        }
                    }
                }
            }

            is ReadingDetailUiState.Success -> {
                ReadingDetailContent(
                    reading = state.reading,
                    modifier = Modifier.padding(paddingValues),
                    onEditClick = { onEditClick(state.reading.id) },
                    onDeleteClick = { showDeleteDialog = true }
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Reading?") },
            text = { Text("Are you sure you want to delete this reading? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteReading()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete progress dialog
    if (deleteState is DeleteState.Deleting) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Deleting...") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            confirmButton = { }
        )
    }

    // Delete error snackbar
    if (deleteState is DeleteState.Error) {
        LaunchedEffect(deleteState) {
            // Show error - in production, use SnackbarHost
            viewModel.resetDeleteState()
        }
    }
}

@Composable
private fun ReadingDetailContent(
    reading: GlucoseReading,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Large glucose value display with color indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row {
                // Color indicator bar
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .fillMaxHeight()
                        .background(getLevelColor(reading.color))
                )

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large glucose value
                    Text(
                        text = reading.reading.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = reading.units,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Level description
                    Text(
                        text = getLevelDescription(reading.color),
                        style = MaterialTheme.typography.titleMedium,
                        color = getLevelColor(reading.color)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reading details
        DetailRow(label = "Name", value = reading.name)

        DetailRow(
            label = "Date & Time",
            value = formatFullTimestamp(reading.timestamp)
        )

        reading.comment?.let { comment ->
            DetailRow(label = "Comment", value = comment)
        }

        DetailRow(label = "Source", value = reading.source.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        })

        // Sync status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sync Status",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!reading.synced) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Not synced",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = if (reading.synced) "Synced" else "Not Synced",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (reading.synced)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Edit button (placeholder for future implementation)
            OutlinedButton(
                onClick = onEditClick,
                modifier = Modifier.weight(1f),
                enabled = false // TODO: Implement edit functionality
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit")
            }

            // Delete button
            Button(
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
    Divider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    )
}

private fun getLevelColor(color: String?): Color {
    return when (color?.lowercase()) {
        "green" -> GlucoseGreen
        "orange" -> GlucoseOrange
        "red" -> GlucoseRed
        "purple" -> GlucosePurple
        else -> Color.Gray
    }
}

private fun getLevelDescription(color: String?): String {
    return when (color?.lowercase()) {
        "green" -> "Normal Range"
        "orange" -> "Elevated"
        "red" -> "High"
        "purple" -> "Critical"
        else -> "Unknown"
    }
}

private fun formatFullTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("EEEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
