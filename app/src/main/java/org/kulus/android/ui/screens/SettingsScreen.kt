package org.kulus.android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.kulus.android.data.model.GlucoseUnit
import org.kulus.android.data.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onSignedOut: () -> Unit = {},
    hideTopBar: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle action states
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is ActionState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            is ActionState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetActionState()
            }
            is ActionState.SignedOut -> {
                onSignedOut()
            }
            else -> { }
        }
    }

    Scaffold(
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = { Text("Settings") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is SettingsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            is SettingsUiState.Success -> {
                SettingsContent(
                    preferences = state.preferences,
                    modifier = Modifier.padding(paddingValues),
                    viewModel = viewModel,
                    onClearDataClick = { showClearDataDialog = true },
                    onSignOutClick = { showSignOutDialog = true },
                    onApiKeyClick = { showApiKeyDialog = true }
                )
            }
        }
    }

    // Dialogs
    if (showClearDataDialog) {
        ClearDataDialog(
            onConfirm = {
                showClearDataDialog = false
                viewModel.clearLocalData()
            },
            onDismiss = { showClearDataDialog = false }
        )
    }

    if (showSignOutDialog) {
        SignOutDialog(
            onConfirm = {
                showSignOutDialog = false
                viewModel.signOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    if (showApiKeyDialog && uiState is SettingsUiState.Success) {
        ApiKeyDialog(
            currentKey = (uiState as SettingsUiState.Success).preferences.openAiApiKey,
            onSave = { apiKey ->
                showApiKeyDialog = false
                viewModel.updateOpenAiApiKey(apiKey)
            },
            onDismiss = { showApiKeyDialog = false }
        )
    }

    // Loading overlay
    if (actionState is ActionState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (actionState as ActionState.Loading).message,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    preferences: org.kulus.android.data.preferences.UserPreferences,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel,
    onClearDataClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onApiKeyClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Section
        SettingsSectionHeader("Profile")

        var nameText by remember { mutableStateOf(preferences.defaultName) }
        SettingsTextField(
            label = "Default Name",
            value = nameText,
            onValueChange = { nameText = it },
            onDone = { viewModel.updateDefaultName(nameText) },
            icon = Icons.Default.Person
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // Glucose Settings
        SettingsSectionHeader("Glucose Settings")

        SettingsOption(
            title = "Preferred Unit",
            subtitle = preferences.preferredUnit.displayName,
            icon = Icons.Default.BarChart
        ) {
            var expanded by remember { mutableStateOf(false) }

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Preferred Unit",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = preferences.preferredUnit.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                if (expanded) {
                    Column(modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 8.dp)) {
                        GlucoseUnit.values().forEach { unit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updatePreferredUnit(unit)
                                        expanded = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = preferences.preferredUnit == unit,
                                    onClick = {
                                        viewModel.updatePreferredUnit(unit)
                                        expanded = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = unit.displayName)
                            }
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // Appearance Section
        SettingsSectionHeader("Appearance")

        SettingsOption(
            title = "Theme",
            subtitle = preferences.themeMode.displayName,
            icon = Icons.Default.DarkMode
        ) {
            var expanded by remember { mutableStateOf(false) }

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Theme",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = preferences.themeMode.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                if (expanded) {
                    Column(modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 8.dp)) {
                        ThemeMode.values().forEach { mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.updateThemeMode(mode)
                                        expanded = false
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = preferences.themeMode == mode,
                                    onClick = {
                                        viewModel.updateThemeMode(mode)
                                        expanded = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = mode.displayName)
                            }
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // Advanced Section
        SettingsSectionHeader("Advanced")

        SettingsClickableOption(
            title = "OpenAI API Key",
            subtitle = if (preferences.openAiApiKey.isNullOrBlank()) "Not set" else "••••••••",
            icon = Icons.Default.Key,
            onClick = onApiKeyClick
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // Data Section
        SettingsSectionHeader("Data Management")

        SettingsClickableOption(
            title = "Clear Local Data",
            subtitle = "Remove all local glucose readings",
            icon = Icons.Default.Delete,
            onClick = onClearDataClick,
            danger = true
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // Account Section
        SettingsSectionHeader("Account")

        SettingsClickableOption(
            title = "Sign Out",
            subtitle = "Clear credentials and local data",
            icon = Icons.Default.Logout,
            onClick = onSignOutClick,
            danger = true
        )

        Divider(modifier = Modifier.padding(horizontal = 16.dp))

        // About Section
        SettingsSectionHeader("About")

        SettingsInfoOption(
            title = "Version",
            subtitle = viewModel.getAppVersion(),
            icon = Icons.Default.Info
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        fontWeight = FontWeight.Bold
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        IconButton(onClick = onDone) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Save",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SettingsOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    content()
}

@Composable
private fun SettingsClickableOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsInfoOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ClearDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear Local Data?") },
        text = { Text("This will permanently delete all glucose readings stored on this device. This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear Data")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SignOutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign Out?") },
        text = { Text("This will clear your credentials and all local data. You will need to sign in again.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sign Out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApiKeyDialog(
    currentKey: String?,
    onSave: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyText by remember { mutableStateOf(currentKey ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("OpenAI API Key") },
        text = {
            Column {
                Text(
                    text = "Optional: Add your OpenAI API key for enhanced OCR verification.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("sk-...") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(apiKeyText.ifBlank { null }) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
