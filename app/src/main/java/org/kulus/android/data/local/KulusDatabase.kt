package org.kulus.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.kulus.android.data.model.GlucoseReading

@Database(
    entities = [GlucoseReading::class],
    version = 3,
    exportSchema = false
)
abstract class KulusDatabase : RoomDatabase() {
    abstract fun glucoseReadingDao(): GlucoseReadingDao

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
    }
}
