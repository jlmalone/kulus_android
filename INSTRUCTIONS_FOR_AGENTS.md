# Instructions for Remote Agents

## 📋 Your Task: Build the UI for Kulus Android

This is a **glucose monitoring Android app** with a complete backend integration already implemented. Your job is to build the **user interface screens** using **Jetpack Compose**.

## ✅ What's Already Done (Don't Touch)

- ✅ Complete data layer (API client, Room database, Repository)
- ✅ Authentication system
- ✅ Dependency injection (Hilt)
- ✅ Beautiful Matrix neon theme (from REDO Android)
- ✅ All data models and API integration
- ✅ Offline-first architecture

## 🎯 What You Need to Build

### Phase 1: Core Screens (Start Here)

#### Step 1: ReadingsViewModel
**File**: `app/src/main/java/org/kulus/android/ui/screens/ReadingsViewModel.kt`

```kotlin
package org.kulus.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.data.repository.KulusRepository
import javax.inject.Inject

@HiltViewModel
class ReadingsViewModel @Inject constructor(
    private val repository: KulusRepository
) : ViewModel() {

    val readings: StateFlow<List<GlucoseReading>> = repository
        .getAllReadingsLocal()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun syncFromServer() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.syncReadingsFromServer()
                .onSuccess {
                    _errorMessage.value = null
                }
                .onFailure { error ->
                    _errorMessage.value = error.message
                }
            _isRefreshing.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
```

#### Step 2: GlucoseReadingCard Component
**File**: `app/src/main/java/org/kulus/android/ui/components/GlucoseReadingCard.kt`

```kotlin
package org.kulus.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GlucoseReadingCard(
    reading: GlucoseReading,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row {
            // Color indicator bar on left
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(getLevelColor(reading.color))
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f)
            ) {
                // Glucose value (large and prominent)
                Text(
                    text = "${reading.reading} ${reading.units}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Name
                Text(
                    text = reading.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Timestamp
                Text(
                    text = formatTimestamp(reading.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Comment (if present)
                reading.comment?.let { comment ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = comment,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sync status
                if (!reading.synced) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Not synced",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Not synced",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
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

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
```

#### Step 3: ReadingsListScreen
**File**: `app/src/main/java/org/kulus/android/ui/screens/ReadingsListScreen.kt`

```kotlin
package org.kulus.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.kulus.android.ui.components.GlucoseReadingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingsListScreen(
    viewModel: ReadingsViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {},
    onReadingClick: (String) -> Unit = {}
) {
    val readings by viewModel.readings.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kulus") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add reading")
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { viewModel.syncFromServer() },
            modifier = Modifier.padding(paddingValues)
        ) {
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

        // Error message snackbar
        errorMessage?.let { message ->
            LaunchedEffect(message) {
                // Show snackbar (simplified - implement proper SnackbarHost)
            }
        }
    }
}
```

**Note**: You need to add this dependency to `app/build.gradle.kts`:
```kotlin
implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")
```

#### Step 4: AddReadingViewModel
**File**: `app/src/main/java/org/kulus/android/ui/screens/AddReadingViewModel.kt`

```kotlin
package org.kulus.android.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.kulus.android.data.model.GlucoseUnit
import org.kulus.android.data.repository.KulusRepository
import javax.inject.Inject

@HiltViewModel
class AddReadingViewModel @Inject constructor(
    private val repository: KulusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddReadingUiState>(AddReadingUiState.Idle)
    val uiState: StateFlow<AddReadingUiState> = _uiState.asStateFlow()

    fun addReading(
        reading: Double,
        name: String,
        units: GlucoseUnit,
        comment: String?,
        snackPass: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = AddReadingUiState.Loading

            repository.addReading(
                reading = reading,
                name = name,
                units = units,
                comment = comment.takeIf { !it.isNullOrBlank() },
                snackPass = snackPass
            )
                .onSuccess {
                    _uiState.value = AddReadingUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = AddReadingUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun resetState() {
        _uiState.value = AddReadingUiState.Idle
    }
}

sealed interface AddReadingUiState {
    object Idle : AddReadingUiState
    object Loading : AddReadingUiState
    object Success : AddReadingUiState
    data class Error(val message: String) : AddReadingUiState
}
```

#### Step 5: AddReadingScreen
**File**: `app/src/main/java/org/kulus/android/ui/screens/AddReadingScreen.kt`

```kotlin
package org.kulus.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.kulus.android.data.model.GlucoseUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReadingScreen(
    viewModel: AddReadingViewModel = hiltViewModel(),
    onSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var glucoseValue by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf(GlucoseUnit.MMOL_L) }
    var snackPass by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()

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
```

#### Step 6: Navigation Setup
**File**: Update `MainActivity.kt`

```kotlin
package org.kulus.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.kulus.android.ui.screens.AddReadingScreen
import org.kulus.android.ui.screens.ReadingsListScreen
import org.kulus.android.ui.theme.KulusTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KulusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KulusApp()
                }
            }
        }
    }
}

@Composable
fun KulusApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "readings_list"
    ) {
        composable("readings_list") {
            ReadingsListScreen(
                onAddClick = { navController.navigate("add_reading") },
                onReadingClick = { id ->
                    // TODO: Implement reading detail screen
                }
            )
        }

        composable("add_reading") {
            AddReadingScreen(
                onSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
```

## 🚀 Testing Your Work

### 1. Build the app
```bash
./gradlew assembleDebug
```

### 2. Install on device/emulator
```bash
./gradlew installDebug
```

### 3. Test the flow
1. App should open to an empty readings list
2. Tap the + button to add a reading
3. Fill in the form and submit
4. Should navigate back and show the new reading
5. Pull to refresh should sync with the server

### 4. Check logs
```bash
adb logcat | grep -E "KulusRepository|ReadingsViewModel"
```

## 📚 Important Notes

1. **Don't modify the data layer** - It's complete and tested
2. **Use the existing theme** - All colors are defined in `ui/theme/`
3. **Follow the examples** - The code snippets above are copy-paste ready
4. **Test incrementally** - Build after each file you create
5. **Check CLAUDE.md** - More detailed examples and patterns

## ✅ Done Checklist

- [ ] Created ReadingsViewModel.kt
- [ ] Created GlucoseReadingCard.kt
- [ ] Created ReadingsListScreen.kt
- [ ] Created AddReadingViewModel.kt
- [ ] Created AddReadingScreen.kt
- [ ] Updated MainActivity.kt with Navigation
- [ ] Added swiperefresh dependency to build.gradle.kts
- [ ] Built and tested the app
- [ ] Verified readings sync to Firebase

## 🆘 If You Get Stuck

1. Check `CLAUDE.md` for detailed implementation guidance
2. Check `PROJECT_STATUS.md` for architecture overview
3. Look at existing files in `data/` for reference patterns
4. Build errors? Check that all imports are correct
5. Runtime errors? Check `adb logcat` for stack traces

---

**Start with Phase 1 and work through each step in order. Good luck!**
