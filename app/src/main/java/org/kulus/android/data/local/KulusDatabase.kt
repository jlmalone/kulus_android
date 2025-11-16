package org.kulus.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.kulus.android.data.model.GlucoseReading

@Database(
    entities = [GlucoseReading::class],
    version = 1,
    exportSchema = false
)
abstract class KulusDatabase : RoomDatabase() {
    abstract fun glucoseReadingDao(): GlucoseReadingDao
}
