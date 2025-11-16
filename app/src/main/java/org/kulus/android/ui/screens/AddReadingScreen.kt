package org.kulus.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.LocalOwnersProvider
import org.kulus.android.data.model.GlucoseUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingScreen(
    viewModel: AddReadingViewModel = hiltViewModel(),
    onSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onScanClick: () -> Unit = {},
    scannedValue: Double? = null,
    scannedUnit: String? = null
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
            // Scan button
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
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "or enter manually",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
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

            // Submit button
            Button(
                onClick = {
                    val reading = glucoseValue.toDoubleOrNull()
                    if (reading != null && name.isNotBlank()) {
                        viewModel.addReading(reading, name, selectedUnit, comment, snackPass)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddReadingUiState.Loading &&
                        glucoseValue.toDoubleOrNull() != null &&
                        name.isNotBlank()
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

            // Error message
            if (uiState is AddReadingUiState.Error) {
                Text(
                    text = (uiState as AddReadingUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
