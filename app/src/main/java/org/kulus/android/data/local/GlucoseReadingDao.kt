package org.kulus.android.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.kulus.android.data.model.GlucoseReading

@Dao
interface GlucoseReadingDao {

    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<GlucoseReading>>

    @Query("SELECT * FROM glucose_readings WHERE id = :id")
    suspend fun getReadingById(id: String): GlucoseReading?

    @Query("SELECT * FROM glucose_readings WHERE name = :name ORDER BY timestamp DESC")
    fun getReadingsByName(name: String): Flow<List<GlucoseReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: GlucoseReading)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<GlucoseReading>)

    @Update
    suspend fun updateReading(reading: GlucoseReading)

    @Delete
    suspend fun deleteReading(reading: GlucoseReading)

    @Query("DELETE FROM glucose_readings")
    suspend fun deleteAllReadings()

    @Query("SELECT COUNT(*) FROM glucose_readings")
    suspend fun getReadingCount(): Int

    @Query("SELECT * FROM glucose_readings WHERE synced = 0")
    suspend fun getUnsyncedReadings(): List<GlucoseReading>

    @Query("UPDATE glucose_readings SET synced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
