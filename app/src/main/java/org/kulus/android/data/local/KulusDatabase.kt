package org.kulus.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.kulus.android.data.model.GlucoseReading
import org.kulus.android.data.model.UserProfile

@Database(
    entities = [GlucoseReading::class, UserProfile::class],
    version = 5,
    exportSchema = false
)
abstract class KulusDatabase : RoomDatabase() {
    abstract fun glucoseReadingDao(): GlucoseReadingDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add photoUri column to glucose_readings table
                database.execSQL("ALTER TABLE glucose_readings ADD COLUMN photoUri TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add tags column to glucose_readings table
                database.execSQL("ALTER TABLE glucose_readings ADD COLUMN tags TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create user_profiles table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_profiles (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        phoneNumber TEXT,
                        defaultUnit TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        avatarColor TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        readingCount INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO user_profiles (
                        id, name, phoneNumber, defaultUnit, isActive,
                        avatarColor, createdAt, readingCount
                    ) VALUES (
                        '00000000-0000-0000-0000-000000000001',
                        'Me',
                        NULL,
                        'mmol/L',
                        1,
                        '#00FFB8',
                        ${System.currentTimeMillis()},
                        0
                    )
                """.trimIndent())

                database.execSQL("ALTER TABLE glucose_readings ADD COLUMN profileId TEXT NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'")

                database.execSQL("""
                    UPDATE user_profiles
                    SET readingCount = (SELECT COUNT(*) FROM glucose_readings WHERE profileId = '00000000-0000-0000-0000-000000000001')
                    WHERE id = '00000000-0000-0000-0000-000000000001'
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Sync queue fields for enterprise-grade offline sync
                database.execSQL("ALTER TABLE glucose_readings ADD COLUMN pendingSync INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE glucose_readings ADD COLUMN syncAttemptCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE glucose_readings ADD COLUMN lastSyncAttempt INTEGER")
                // Device name for Bluetooth readings
                database.execSQL("ALTER TABLE glucose_readings ADD COLUMN deviceName TEXT")
                // Mark existing unsynced readings as pending
                database.execSQL("UPDATE glucose_readings SET pendingSync = 1 WHERE synced = 0")
            }
        }
    }
}
