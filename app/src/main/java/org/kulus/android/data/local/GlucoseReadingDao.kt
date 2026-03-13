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

    @Query("UPDATE glucose_readings SET synced = 1, pendingSync = 0 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("SELECT * FROM glucose_readings WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    suspend fun getReadingsSince(sinceTimestamp: Long): List<GlucoseReading>

    // Enterprise sync queue queries
    @Query("SELECT * FROM glucose_readings WHERE pendingSync = 1 AND syncAttemptCount < :maxRetries ORDER BY timestamp ASC LIMIT :batchSize")
    suspend fun getPendingSyncReadings(maxRetries: Int = 10, batchSize: Int = 50): List<GlucoseReading>

    @Query("UPDATE glucose_readings SET syncAttemptCount = syncAttemptCount + 1, lastSyncAttempt = :timestamp WHERE id = :id")
    suspend fun incrementSyncAttempt(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE glucose_readings SET pendingSync = 1 WHERE id = :id")
    suspend fun markPendingSync(id: String)

    @Query("SELECT COUNT(*) FROM glucose_readings WHERE pendingSync = 1")
    suspend fun getPendingSyncCount(): Int

    @Query("SELECT COUNT(*) FROM glucose_readings WHERE pendingSync = 1")
    fun getPendingSyncCountFlow(): Flow<Int>
}
