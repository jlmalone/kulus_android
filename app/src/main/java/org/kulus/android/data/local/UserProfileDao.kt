package org.kulus.android.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.kulus.android.data.model.UserProfile

/**
 * Data Access Object for User Profiles
 *
 * Provides database operations for multi-user profile management.
 * Supports Flow-based reactive queries for real-time UI updates.
 */
@Dao
interface UserProfileDao {

    /**
     * Get all profiles ordered by creation date (newest first)
     * Returns a Flow for reactive updates
     */
    @Query("SELECT * FROM user_profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<UserProfile>>

    /**
     * Get the currently active profile
     * Should always return exactly one profile
     */
    @Query("SELECT * FROM user_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<UserProfile?>

    /**
     * Get the currently active profile (non-Flow version for one-time reads)
     */
    @Query("SELECT * FROM user_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfileOnce(): UserProfile?

    /**
     * Get a profile by ID
     */
    @Query("SELECT * FROM user_profiles WHERE id = :profileId")
    suspend fun getProfileById(profileId: String): UserProfile?

    /**
     * Insert a new profile
     * Returns the row ID (not used since we use UUIDs)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile): Long

    /**
     * Update an existing profile
     */
    @Update
    suspend fun updateProfile(profile: UserProfile)

    /**
     * Delete a profile
     * WARNING: Should also delete associated glucose readings
     */
    @Delete
    suspend fun deleteProfile(profile: UserProfile)

    /**
     * Set a profile as active and deactivate all others
     * This is a transaction to ensure only one profile is active
     */
    @Transaction
    suspend fun setActiveProfile(profileId: String) {
        // Deactivate all profiles
        deactivateAllProfiles()
        // Activate the selected profile
        activateProfile(profileId)
    }

    /**
     * Deactivate all profiles (used internally by setActiveProfile)
     */
    @Query("UPDATE user_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    /**
     * Activate a specific profile (used internally by setActiveProfile)
     */
    @Query("UPDATE user_profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun activateProfile(profileId: String)

    /**
     * Update the reading count for a profile
     * Called when readings are added/deleted
     */
    @Query("UPDATE user_profiles SET readingCount = :count WHERE id = :profileId")
    suspend fun updateReadingCount(profileId: String, count: Int)

    /**
     * Get count of all profiles
     */
    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getProfileCount(): Int

    /**
     * Check if a profile name already exists
     * Useful for preventing duplicate names
     */
    @Query("SELECT COUNT(*) FROM user_profiles WHERE name = :name")
    suspend fun profileNameExists(name: String): Int
}
