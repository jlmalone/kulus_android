package org.kulus.android.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import org.kulus.android.data.model.GlucoseUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingScreen(
    viewModel: AddReadingViewModel = hiltViewModel(),
    onSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    scannedValue: Double? = null,
    scannedUnit: String? = null,
    scannedPhotoUri: String? = null
) {
    var glucoseValue by remember { mutableStateOf(scannedValue?.toString() ?: "") }
    var name by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var selectedUnit by remember {
        mutableStateOf(
            when (scannedUnit) {
                "mg/dL" -> GlucoseUnit.MG_DL
                "mmol/L" -> GlucoseUnit.MMOL_L
                else -> GlucoseUnit.MMOL_L
            }
        )
    }
    var snackPass by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf(scannedPhotoUri) }
    var source by remember { mutableStateOf(if (scannedPhotoUri != null) "Photo" else "Manual") }

    val uiState by viewModel.uiState.collectAsState()

    // Update glucose value if scanned value changes
    LaunchedEffect(scannedValue) {
        scannedValue?.let {
            glucoseValue = it.toString()
        }
    }

    LaunchedEffect(scannedUnit) {
        scannedUnit?.let {
            selectedUnit = when (it) {
                "mg/dL" -> GlucoseUnit.MG_DL
                "mmol/L" -> GlucoseUnit.MMOL_L
                else -> selectedUnit
            }
        }
    }

    LaunchedEffect(scannedPhotoUri) {
        scannedPhotoUri?.let {
            photoUri = it
            source = "Photo"
        }
    }

    // Handle success navigation
    LaunchedEffect(uiState) {
        if (uiState is AddReadingUiState.Success) {
            onSuccess()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Reading") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo preview (if available)
            photoUri?.let { uri ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = rememberAsyncImagePainter(Uri.parse(uri)),
                            contentDescription = "Scanned photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        // Remove photo button
                        IconButton(
                            onClick = {
                                photoUri = null
                                source = "Manual"
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove photo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Scan button (only show if no photo)
            if (photoUri == null) {
                OutlinedButton(
                    onClick = onScanClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Glucose Reading")
                }

                // Or divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text(
                        text = "or enter manually",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider(modifier = Modifier.weight(1f))
                }
            }

            // Source selector
            Column {
                Text(
                    text = "Source",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = source == "Manual",
                        onClick = { source = "Manual" },
                        label = { Text("Manual Entry") },
                        modifier = Modifier.weight(1f),
                        enabled = photoUri == null
                    )
                    FilterChip(
                        selected = source == "Photo",
                        onClick = { source = "Photo" },
                        label = { Text("Photo Scan") },
                        modifier = Modifier.weight(1f),
                        enabled = photoUri != null
                    )
                }
            }

            // Glucose value input
            OutlinedTextField(
                value = glucoseValue,
                onValueChange = { glucoseValue = it },
                label = { Text("Glucose Value") },
                placeholder = { Text("Enter reading") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Unit selector (Segmented Button)
            Column {
                Text(
                    text = "Unit",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedUnit == GlucoseUnit.MMOL_L,
                        onClick = { selectedUnit = GlucoseUnit.MMOL_L },
                        label = { Text("mmol/L") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedUnit == GlucoseUnit.MG_DL,
                        onClick = { selectedUnit = GlucoseUnit.MG_DL },
                        label = { Text("mg/dL") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Name input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your Name") },
                placeholder = { Text("Enter name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Comment input
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comment (optional)") },
                placeholder = { Text("Add a note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // Snack pass toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Snack Pass",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Suppress SMS alerts for this reading",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = snackPass,
                    onCheckedChange = { snackPass = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Validation errors
            val reading = glucoseValue.toDoubleOrNull()
            val validationError = when {
                glucoseValue.isNotBlank() && reading == null -> "Please enter a valid number"
                reading != null && reading <= 0 -> "Glucose value must be greater than 0"
                reading != null && selectedUnit == GlucoseUnit.MMOL_L && reading > 33.3 -> "Value seems unusually high (max ~33 mmol/L)"
                reading != null && selectedUnit == GlucoseUnit.MG_DL && reading > 600 -> "Value seems unusually high (max ~600 mg/dL)"
                name.isBlank() -> null // Don't show name error until they try to submit
                else -> null
            }

            // Show validation error
            if (validationError != null) {
                Text(
                    text = validationError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Submit button
            Button(
                onClick = {
                    val validReading = glucoseValue.toDoubleOrNull()
                    if (validReading != null && validReading > 0 && name.isNotBlank()) {
                        viewModel.addReading(
                            reading = validReading,
                            name = name,
                            units = selectedUnit,
                            comment = comment,
                            snackPass = snackPass,
                            photoUri = photoUri,
                            source = source.lowercase()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddReadingUiState.Loading &&
                        reading != null &&
                        reading > 0 &&
                        name.isNotBlank() &&
                        validationError == null
            ) {
                if (uiState is AddReadingUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit")
                }
            }

            // Error message from API
            if (uiState is AddReadingUiState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (uiState as AddReadingUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
