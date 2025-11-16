package org.kulus.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kulus_prefs")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val TOKEN_EXPIRY_KEY = longPreferencesKey("token_expiry")

    suspend fun saveToken(token: String, expiresIn: Long) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[TOKEN_EXPIRY_KEY] = System.currentTimeMillis() + expiresIn
        }
    }

    fun getToken(): Flow<String?> = context.dataStore.data.map { preferences ->
        val token = preferences[TOKEN_KEY]
        val expiry = preferences[TOKEN_EXPIRY_KEY] ?: 0L

        // Return null if token is expired
        if (System.currentTimeMillis() > expiry) {
            null
        } else {
            token
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(TOKEN_EXPIRY_KEY)
        }
    }

    fun isTokenValid(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        val expiry = preferences[TOKEN_EXPIRY_KEY] ?: 0L
        System.currentTimeMillis() < expiry
    }
}
