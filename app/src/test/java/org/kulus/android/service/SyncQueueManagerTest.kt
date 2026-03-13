package org.kulus.android.service

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.min
import kotlin.math.pow

/**
 * Tests for the exponential backoff logic in SyncQueueManager.
 * We test the algorithm directly since the actual SyncQueueManager
 * requires Android dependencies (Room DAO, API service, etc.).
 */
class SyncQueueManagerTest {

    companion object {
        private const val MAX_BACKOFF_SECONDS = 300L // 5 minutes cap, matches SyncQueueManager
    }

    /**
     * Mirrors SyncQueueManager.calculateBackoffMs() exactly.
     */
    private fun calculateBackoffMs(attemptCount: Int): Long {
        val backoffSeconds = min(MAX_BACKOFF_SECONDS, 2.0.pow(attemptCount).toLong())
        return backoffSeconds * 1000
    }

    /**
     * Mirrors SyncQueueManager.isEligibleForRetry() exactly.
     */
    private fun isEligibleForRetry(lastAttempt: Long?, attemptCount: Int, currentTime: Long): Boolean {
        if (lastAttempt == null) return true
        val backoffMs = calculateBackoffMs(attemptCount)
        return currentTime - lastAttempt >= backoffMs
    }

    // --- Exponential backoff calculation ---

    @Test
    fun `backoff - attempt 0 is 1 second`() {
        assertEquals(1_000L, calculateBackoffMs(0))
    }

    @Test
    fun `backoff - attempt 1 is 2 seconds`() {
        assertEquals(2_000L, calculateBackoffMs(1))
    }

    @Test
    fun `backoff - attempt 2 is 4 seconds`() {
        assertEquals(4_000L, calculateBackoffMs(2))
    }

    @Test
    fun `backoff - attempt 3 is 8 seconds`() {
        assertEquals(8_000L, calculateBackoffMs(3))
    }

    @Test
    fun `backoff - attempt 4 is 16 seconds`() {
        assertEquals(16_000L, calculateBackoffMs(4))
    }

    @Test
    fun `backoff - attempt 5 is 32 seconds`() {
        assertEquals(32_000L, calculateBackoffMs(5))
    }

    @Test
    fun `backoff - attempt 8 is 256 seconds`() {
        assertEquals(256_000L, calculateBackoffMs(8))
    }

    @Test
    fun `backoff - attempt 9 caps at 300 seconds`() {
        // 2^9 = 512 > 300, so cap at 300
        assertEquals(300_000L, calculateBackoffMs(9))
    }

    @Test
    fun `backoff - attempt 10 caps at 300 seconds`() {
        assertEquals(300_000L, calculateBackoffMs(10))
    }

    @Test
    fun `backoff - very high attempt still caps at 300 seconds`() {
        assertEquals(300_000L, calculateBackoffMs(20))
    }

    // --- Retry eligibility ---

    @Test
    fun `isEligibleForRetry - null lastAttempt always eligible`() {
        assertTrue(isEligibleForRetry(null, 0, System.currentTimeMillis()))
    }

    @Test
    fun `isEligibleForRetry - recent attempt not eligible`() {
        val now = 100_000L
        val lastAttempt = 99_500L // 500ms ago, need 1000ms for attempt 0
        assertFalse(isEligibleForRetry(lastAttempt, 0, now))
    }

    @Test
    fun `isEligibleForRetry - elapsed backoff is eligible`() {
        val now = 100_000L
        val lastAttempt = 98_000L // 2000ms ago, need 1000ms for attempt 0
        assertTrue(isEligibleForRetry(lastAttempt, 0, now))
    }

    @Test
    fun `isEligibleForRetry - high attempt needs longer backoff`() {
        val now = 100_000L
        // Attempt 3 needs 8s backoff
        val lastAttempt = 95_000L // 5s ago, need 8s
        assertFalse(isEligibleForRetry(lastAttempt, 3, now))
    }

    @Test
    fun `isEligibleForRetry - high attempt with enough time is eligible`() {
        val now = 100_000L
        // Attempt 3 needs 8s backoff
        val lastAttempt = 91_000L // 9s ago, need 8s
        assertTrue(isEligibleForRetry(lastAttempt, 3, now))
    }
}
