# Kulus Android - Rich Onboarding Experience

## Current Problems

1. ❌ Main app loads in background during onboarding (shows "Latest Reading")
2. ❌ Single dialog is not engaging or informative
3. ❌ No explanation of app features or privacy
4. ❌ Doesn't match iOS onboarding quality
5. ❌ Purple color issue (needs investigation)

## Proposed Solution: Full-Screen Onboarding Flow

### Architecture Changes

#### 1. Navigation Structure
```kotlin
// Add to MainActivity or create OnboardingActivity
sealed class AppState {
    object NeedsOnboarding : AppState()
    object Ready : AppState()
}

// MainActivity.kt
@Composable
fun KulusApp() {
    val prefsRepository: PreferencesRepository = hiltViewModel<MainViewModel>().prefsRepository
    val prefs by prefsRepository.userPreferencesFlow.collectAsState(initial = UserPreferences())

    // Check if onboarding is needed
    val needsOnboarding = prefs.defaultName == "mobile-user"

    if (needsOnboarding) {
        OnboardingFlow(
            onComplete = { name ->
                // Save name and proceed
            }
        )
    } else {
        DashboardScreen() // Main app
    }
}
```

#### 2. Onboarding Flow Screens

**Screen 1: Welcome**
```kotlin
@Composable
fun WelcomeScreen(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hamumu butterfly logo (large)
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Kulus Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to Kulus",
            style = MaterialTheme.typography.headlineLarge,
            color = MatrixNeon
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Track your glucose readings securely",
            style = MaterialTheme.typography.bodyLarge,
            color = MatrixTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Started")
        }
    }
}
```

**Screen 2: Features Overview**
```kotlin
@Composable
fun FeaturesScreen(onNext: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
    ) {
        Text(
            text = "What Kulus Does",
            style = MaterialTheme.typography.headlineMedium,
            color = MatrixNeon
        )

        Spacer(modifier = Modifier.height(32.dp))

        FeatureItem(
            icon = Icons.Default.PhotoCamera,
            title = "Photo OCR",
            description = "Scan glucose meter screens automatically"
        )

        FeatureItem(
            icon = Icons.Default.TrendingUp,
            title = "Trends & Analytics",
            description = "View charts, statistics, and A1C estimates"
        )

        FeatureItem(
            icon = Icons.Default.CloudSync,
            title = "Cloud Sync",
            description = "Your data syncs across all your devices"
        )

        FeatureItem(
            icon = Icons.Default.Lock,
            title = "Privacy First",
            description = "Your data is private and encrypted"
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
            Button(onClick = onNext) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun FeatureItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MatrixNeon,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MatrixTextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MatrixTextSecondary
            )
        }
    }
}
```

**Screen 3: Privacy & Data Segregation**
```kotlin
@Composable
fun PrivacyScreen(onNext: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MatrixNeon,
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Your Privacy Matters",
            style = MaterialTheme.typography.headlineMedium,
            color = MatrixNeon,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrivacyPoint(
            "🔒 Data Segregation",
            "You will only see YOUR glucose readings, never anyone else's"
        )

        PrivacyPoint(
            "🔑 User Identifier",
            "Your name/phone number is used to keep your data separate from other users"
        )

        PrivacyPoint(
            "📱 Multi-Device",
            "Use the SAME name on all your devices to sync your data"
        )

        PrivacyPoint(
            "☁️ Cloud Storage",
            "Data is backed up to Firebase with encryption"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = GlucoseRed.copy(alpha = 0.1f)
            )
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = GlucoseRed,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "IMPORTANT: Remember your name/phone number. You'll need it on other devices!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MatrixTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
            Button(onClick = onNext) {
                Text("Set Up Account")
            }
        }
    }
}

@Composable
fun PrivacyPoint(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MatrixAmber
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MatrixTextSecondary
            )
        }
    }
}
```

**Screen 4: Account Setup**
```kotlin
@Composable
fun AccountSetupScreen(
    onComplete: (String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var confirmName by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
    ) {
        Text(
            text = "Set Up Your Account",
            style = MaterialTheme.typography.headlineMedium,
            color = MatrixNeon
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enter your name or phone number",
            style = MaterialTheme.typography.bodyLarge,
            color = MatrixTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This will be used to identify your data. Use the same identifier on all your devices.",
            style = MaterialTheme.typography.bodyMedium,
            color = MatrixTextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                showError = false
            },
            label = { Text("Name or Phone Number") },
            placeholder = { Text("e.g., John Smith or (555) 123-4567") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MatrixNeon,
                focusedLabelColor = MatrixNeon
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmName,
            onValueChange = {
                confirmName = it
                showError = false
            },
            label = { Text("Confirm Name or Phone Number") },
            placeholder = { Text("Type it again to confirm") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = showError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MatrixNeon,
                focusedLabelColor = MatrixNeon,
                errorBorderColor = GlucoseRed,
                errorLabelColor = GlucoseRed
            )
        )

        if (showError) {
            Text(
                text = "Names don't match. Please try again.",
                color = GlucoseRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MatrixAmber.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MatrixAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Examples:\n• Full name: \"Sarah Johnson\"\n• Phone: \"(510) 555-1234\"\n• Email: \"sarah@example.com\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MatrixTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
            Button(
                onClick = {
                    when {
                        name.isBlank() -> showError = true
                        name != confirmName -> showError = true
                        name.length < 3 -> showError = true
                        else -> onComplete(name.trim())
                    }
                },
                enabled = name.isNotBlank() && confirmName.isNotBlank()
            ) {
                Text("Complete Setup")
            }
        }
    }
}
```

**Screen 5: Completion**
```kotlin
@Composable
fun CompletionScreen(userName: String, onFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GlucoseGreen,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "All Set!",
            style = MaterialTheme.typography.headlineLarge,
            color = MatrixNeon
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome, $userName",
            style = MaterialTheme.typography.titleLarge,
            color = MatrixTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your account has been created and you're ready to track your glucose readings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MatrixTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
```

**Main Onboarding Coordinator**
```kotlin
@Composable
fun OnboardingFlow(
    onComplete: (String) -> Unit
) {
    var currentScreen by remember { mutableStateOf(0) }
    var userName by remember { mutableStateOf("") }

    when (currentScreen) {
        0 -> WelcomeScreen(
            onNext = { currentScreen = 1 }
        )
        1 -> FeaturesScreen(
            onNext = { currentScreen = 2 },
            onBack = { currentScreen = 0 }
        )
        2 -> PrivacyScreen(
            onNext = { currentScreen = 3 },
            onBack = { currentScreen = 1 }
        )
        3 -> AccountSetupScreen(
            onComplete = { name ->
                userName = name
                currentScreen = 4
            },
            onBack = { currentScreen = 2 }
        )
        4 -> CompletionScreen(
            userName = userName,
            onFinish = { onComplete(userName) }
        )
    }
}
```

### Implementation Steps

1. Create new file: `ui/onboarding/OnboardingFlow.kt`
2. Update `MainActivity.kt` to check onboarding status
3. Add paging indicator at bottom of each screen
4. Add animations between screens (slide transitions)
5. Test with clean app data to verify flow

### Color Investigation

**Check these files for purple issues:**
- `ui/theme/Color.kt` - Look for purple definitions
- `ui/screens/DashboardScreen.kt` - Check navigation bar colors
- `ui/components/GlucoseReadingCard.kt` - Check glucose level colors

**Common purple sources:**
- Material3 default primary color (should be MatrixNeon)
- Glucose level "Critical" = GlucosePurple
- Unselected navigation items

### Testing Checklist

- [ ] Onboarding shows on fresh install (clear app data)
- [ ] Cannot access main app without completing setup
- [ ] Name validation works (min 3 chars, matching confirmation)
- [ ] Back navigation works on all screens
- [ ] Can't skip onboarding (no dismiss buttons)
- [ ] Name saves correctly to preferences
- [ ] App proceeds to dashboard after completion
- [ ] No background content visible during onboarding
- [ ] Matrix theme colors used throughout
- [ ] Smooth animations between screens
