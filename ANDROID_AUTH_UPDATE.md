# Android Client - Authentication Update Guide

**Prerequisites:** Backend security fix must be deployed first (see `~/WebstormProjects/Kulus-App/SECURITY_IMPLEMENTATION_GUIDE.md`)

## Overview

Update Android client to pass userName during authentication and remove name parameters from API calls.

## Files to Modify

```
app/src/main/java/org/kulus/android/
├── data/model/
│   ├── AuthRequest.kt          ← MODIFY: Add userName
│   └── AuthResponse.kt         ← MODIFY: Add userId, userName
├── data/api/KulusApiService.kt ← MODIFY: Remove name parameters
├── data/local/TokenStore.kt    ← MODIFY: Save userId, userName
└── data/repository/KulusRepository.kt ← MODIFY: Update auth flow
```

## Step 1: Update AuthRequest.kt

```kotlin
// Location: data/model/AuthRequest.kt

data class AuthRequest(
    val password: String,
    val userName: String  // NEW: User's name/phone number
)
```

## Step 2: Update AuthResponse.kt

```kotlin
// Location: data/model/AuthResponse.kt

data class AuthResponse(
    val success: Boolean,
    val token: String,
    val expiresIn: Long,
    val userId: String,      // NEW: Unique user ID (hash)
    val userName: String     // NEW: User's display name
)
```

## Step 3: Update KulusApiService.kt

Remove `name` parameters from all endpoints:

```kotlin
// Location: data/api/KulusApiService.kt

interface KulusApiService {

    @POST("/validatePassword")
    suspend fun authenticate(
        @Body request: AuthRequest  // Now includes userName
    ): Response<AuthResponse>

    // REMOVED name parameter - server uses token identity
    @GET("/reportingApiV2")
    suspend fun getReadingsByName(
        @Query("action") action: String = "readings"
        // REMOVED: @Query("name") name: String
    ): Response<KulusReadingsResponse>

    // REMOVED name parameter - server uses token identity
    @GET("/api/v2/addReadingFromUrl")
    suspend fun addReading(
        // REMOVED: @Query("name") name: String
        @Query("reading") reading: String,
        @Query("units") units: String = "mmol/L",
        @Query("comment") comment: String? = null,
        @Query("snackPass") snackPass: Boolean = false,
        @Query("source") source: String = "android"
    ): Response<AddReadingResponse>
}
```

## Step 4: Update TokenStore.kt

Add methods to save/retrieve user identity:

```kotlin
// Location: data/local/TokenStore.kt

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val TOKEN = stringPreferencesKey("auth_token")
        val TOKEN_EXPIRY = longPreferencesKey("token_expiry")
        val USER_ID = stringPreferencesKey("user_id")          // NEW
        val USER_NAME = stringPreferencesKey("user_name")      // NEW
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "token_store"
    )

    suspend fun saveToken(token: String, expiresIn: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOKEN] = token
            preferences[PreferencesKeys.TOKEN_EXPIRY] = System.currentTimeMillis() + expiresIn
        }
    }

    // NEW: Save user identity
    suspend fun saveUserIdentity(userId: String, userName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.USER_NAME] = userName
        }
    }

    // NEW: Get user identity
    fun getUserIdentity(): Flow<Pair<String?, String?>> {
        return context.dataStore.data.map { preferences ->
            Pair(
                preferences[PreferencesKeys.USER_ID],
                preferences[PreferencesKeys.USER_NAME]
            )
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.TOKEN)
            preferences.remove(PreferencesKeys.TOKEN_EXPIRY)
            preferences.remove(PreferencesKeys.USER_ID)        // NEW
            preferences.remove(PreferencesKeys.USER_NAME)      // NEW
        }
    }

    // ... existing methods ...
}
```

## Step 5: Update KulusRepository.kt

Modify authentication to pass userName:

```kotlin
// Location: data/repository/KulusRepository.kt

@Singleton
class KulusRepository @Inject constructor(
    private val apiService: KulusApiService,
    private val glucoseReadingDao: GlucoseReadingDao,
    private val tokenStore: TokenStore,
    private val preferencesRepository: PreferencesRepository
) {

    // Update authenticate to require userName
    suspend fun authenticate(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Get userName from preferences
            val userPrefs = preferencesRepository.userPreferencesFlow.first()
            val userName = userPrefs.defaultName

            if (userName == "mobile-user") {
                return@withContext Result.failure(
                    Exception("User setup required. Please set your name in onboarding.")
                )
            }

            android.util.Log.d("KulusRepository", "🔒 Authenticating as: $userName")

            val response = apiService.authenticate(
                AuthRequest(
                    password = BuildConfig.API_PASSWORD,
                    userName = userName  // NEW: Include user's name
                )
            )

            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true) {
                    // Save token AND user identity
                    tokenStore.saveToken(
                        authResponse.token,
                        authResponse.expiresIn
                    )
                    tokenStore.saveUserIdentity(
                        authResponse.userId,
                        authResponse.userName
                    )

                    android.util.Log.d(
                        "KulusRepository",
                        "✅ Authenticated: ${authResponse.userName} (${authResponse.userId})"
                    )

                    Result.success(true)
                } else {
                    Result.failure(Exception("Authentication failed"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("KulusRepository", "❌ Auth failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // syncReadingsFromServer - no changes needed (already updated)
    // Server now uses token identity automatically

    // addReading - remove name parameter usage
    suspend fun addReading(
        reading: Double,
        units: GlucoseUnit = GlucoseUnit.MMOL_L,
        comment: String? = null,
        snackPass: Boolean = false,
        photoUri: String? = null,
        source: String = "android"
    ): Result<GlucoseReading> = withContext(Dispatchers.IO) {
        try {
            // Get userName for local storage only
            val userPrefs = preferencesRepository.userPreferencesFlow.first()
            val userName = userPrefs.defaultName

            // Create local reading
            val localReading = GlucoseReading(
                id = UUID.randomUUID().toString(),
                reading = reading,
                units = units.apiValue,
                name = userName,  // For local storage
                comment = comment,
                snackPass = snackPass,
                source = source,
                timestamp = System.currentTimeMillis(),
                synced = false,
                photoUri = photoUri
            )

            // Save locally
            glucoseReadingDao.insertReading(localReading)

            // Try to sync to server
            try {
                ensureAuthenticated().getOrThrow()

                val formattedValue = numberFormatter.format(reading)

                // NO name parameter - server uses token identity
                val response = apiService.addReading(
                    reading = formattedValue,
                    units = units.apiValue,
                    comment = comment,
                    snackPass = snackPass,
                    source = source
                )

                if (response.isSuccessful) {
                    val syncedReading = localReading.copy(synced = true)
                    glucoseReadingDao.updateReading(syncedReading)
                    Result.success(syncedReading)
                } else {
                    Result.success(localReading)
                }
            } catch (e: Exception) {
                // Failed to sync but saved locally
                android.util.Log.w("KulusRepository", "Failed to sync: ${e.message}")
                Result.success(localReading)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Step 6: Testing

### Test Authentication

```kotlin
// In ViewModel or test
viewModelScope.launch {
    val result = repository.authenticate()
    result.onSuccess {
        println("✅ Authenticated successfully")
    }.onFailure { error ->
        println("❌ Auth failed: ${error.message}")
    }
}
```

### Test Data Segregation

1. **Clear app data** to trigger onboarding
2. Enter user name "Alice"
3. Add some readings
4. Sync to server
5. **Clear app data** again
6. Enter different name "Bob"
7. Add readings
8. Sync to server
9. Verify Bob doesn't see Alice's readings

### Verify Logs

Look for these log messages:

```
D/KulusRepository: 🔒 Authenticating as: Alice
D/KulusRepository: ✅ Authenticated: Alice (abc123...)
D/KulusRepository: 🔒 [SYNC] Fetching readings for user: Alice
D/KulusRepository: ✅ [SYNC] Fetched 5 readings from Kulus for Alice
```

## Build and Deploy

```bash
cd ~/StudioProjects/kulus_android
./gradlew clean assembleDebug
./gradlew installDebug

# Or build release
./gradlew assembleRelease
```

## Troubleshooting

### "User setup required" error

**Cause:** User hasn't completed onboarding
**Fix:** Clear app data and complete onboarding

### 401 Unauthorized

**Cause:** Token expired or invalid
**Fix:** Re-authenticate (happens automatically)

### Still seeing other users' data

**Cause:** Backend not deployed or using old token
**Fix:**
1. Verify backend is deployed
2. Clear app data
3. Re-authenticate with new token format

### Token format error

**Cause:** Backend and client versions mismatch
**Fix:** Ensure both backend and client updates are deployed together

## Migration Notes

### Existing Users

Users with old tokens will need to:
1. Sign out (clears old token)
2. Sign back in (gets new token with userId)

Or just clear app data and re-onboard.

### Database

Existing local readings keep their `name` field.
Server readings get `userId` added via migration script.

## Verification Checklist

- [ ] Onboarding captures user name
- [ ] Authentication includes userName in request
- [ ] AuthResponse includes userId and userName
- [ ] TokenStore saves user identity
- [ ] API calls don't include name parameter
- [ ] Sync only fetches current user's data
- [ ] Add reading saves with current user
- [ ] Multiple users tested (can't see each other's data)
- [ ] Logs show user identity in all operations
- [ ] Build succeeds with no errors

## Next Steps

After authentication update:

1. Test with 3+ different users
2. Verify data segregation works
3. Test multi-device sync (same name)
4. Add error handling for setup issues
5. Add user profile management
6. Consider adding user switching capability
