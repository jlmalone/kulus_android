package org.kulus.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * User Profile entity for multi-user support
 *
 * Allows multiple users to share one device while keeping their glucose readings separate.
 * Each profile has its own settings, preferences, and reading history.
 *
 * Business rules:
 * - Only one profile can be active at a time (isActive = true)
 * - Profile IDs are UUIDs for global uniqueness
 * - Avatar colors distinguish profiles visually
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /**
     * Display name for the profile
     * Examples: "Mom", "Dad", "Sarah", "John"
     */
    val name: String,

    /**
     * Optional phone number for SMS alerts (from onboarding)
     */
    val phoneNumber: String? = null,

    /**
     * Preferred glucose unit for this profile
     * Each user can have their own unit preference
     */
    val defaultUnit: GlucoseUnit = GlucoseUnit.MMOL_L,

    /**
     * Whether this profile is currently active
     * Only one profile should have isActive = true at any time
     */
    val isActive: Boolean = false,

    /**
     * Avatar color for visual distinction
     * Hex color code (e.g., "#00FFB8" for Matrix neon)
     */
    val avatarColor: String = "#00FFB8",

    /**
     * Timestamp when profile was created
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Total number of readings for this profile
     * Updated when readings are added/deleted
     */
    val readingCount: Int = 0
) {
    companion object {
        /**
         * Available avatar colors for profile selection
         * Using Matrix theme colors for consistency
         */
        val AVATAR_COLORS = listOf(
            "#00FFB8",  // Matrix Neon (default)
            "#FFC833",  // Matrix Amber
            "#00D9FF",  // Cyan
            "#FF6B9D",  // Pink
            "#B8FFE6",  // Mint
            "#FF9966",  // Coral
            "#9D84FF",  // Purple
            "#66FF99"   // Green
        )

        /**
         * Create a default profile for first-time users
         */
        fun createDefault(name: String = "Me", phoneNumber: String? = null): UserProfile {
            return UserProfile(
                name = name,
                phoneNumber = phoneNumber,
                isActive = true,  // First profile is active
                avatarColor = AVATAR_COLORS[0]  // Default to Matrix Neon
            )
        }
    }

    /**
     * Get initials for avatar display (first letter of each word)
     * Examples: "John Doe" → "JD", "Sarah" → "S"
     */
    fun getInitials(): String {
        return name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    }
}
