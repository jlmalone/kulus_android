package org.kulus.android.data.model

import org.junit.Assert.*
import org.junit.Test

class GlucoseReadingTest {

    private fun createReading(
        reading: Double,
        units: String = "mmol/L",
        tags: String? = null,
        snackPass: Boolean = false
    ) = GlucoseReading(
        id = "test-id",
        reading = reading,
        units = units,
        name = "Test User",
        tags = tags,
        snackPass = snackPass
    )

    // --- isValidReading ---

    @Test
    fun `isValidReading - normal mmol value is valid`() {
        val reading = createReading(5.5)
        assertTrue(reading.isValidReading)
    }

    @Test
    fun `isValidReading - lower boundary 1_1 mmol is valid`() {
        val reading = createReading(1.1)
        assertTrue(reading.isValidReading)
    }

    @Test
    fun `isValidReading - upper boundary 33_3 mmol is valid`() {
        val reading = createReading(33.3)
        assertTrue(reading.isValidReading)
    }

    @Test
    fun `isValidReading - zero is invalid`() {
        val reading = createReading(0.0)
        assertFalse(reading.isValidReading)
    }

    @Test
    fun `isValidReading - 51 mmol is invalid`() {
        val reading = createReading(51.0)
        assertFalse(reading.isValidReading)
    }

    @Test
    fun `isValidReading - negative value is invalid`() {
        val reading = createReading(-1.0)
        assertFalse(reading.isValidReading)
    }

    @Test
    fun `isValidReading - mgdl value converted correctly`() {
        // 100 mg/dL = ~5.55 mmol/L, which is valid
        val reading = createReading(100.0, units = "mg/dL")
        assertTrue(reading.isValidReading)
    }

    @Test
    fun `isValidReading - very low mgdl is invalid`() {
        // 5 mg/dL = ~0.28 mmol/L, which is < 1.1
        val reading = createReading(5.0, units = "mg/dL")
        assertFalse(reading.isValidReading)
    }

    // --- glucoseRange ---

    @Test
    fun `glucoseRange - low for value below 3_9`() {
        val reading = createReading(3.0)
        assertEquals("low", reading.glucoseRange)
    }

    @Test
    fun `glucoseRange - normal for 3_9`() {
        val reading = createReading(3.9)
        assertEquals("normal", reading.glucoseRange)
    }

    @Test
    fun `glucoseRange - normal for 5_5`() {
        val reading = createReading(5.5)
        assertEquals("normal", reading.glucoseRange)
    }

    @Test
    fun `glucoseRange - normal for 7_8`() {
        val reading = createReading(7.8)
        assertEquals("normal", reading.glucoseRange)
    }

    @Test
    fun `glucoseRange - elevated for 8_0`() {
        val reading = createReading(8.0)
        assertEquals("elevated", reading.glucoseRange)
    }

    @Test
    fun `glucoseRange - elevated for 11_1`() {
        val reading = createReading(11.1)
        assertEquals("elevated", reading.glucoseRange)
    }

    @Test
    fun `glucoseRange - high for value above 11_1`() {
        val reading = createReading(15.0)
        assertEquals("high", reading.glucoseRange)
    }

    // --- Tags parsing ---

    @Test
    fun `getTagsList - parses comma-separated tags`() {
        val reading = createReading(5.5, tags = "Fasting,Pre-Meal,Exercise")
        assertEquals(listOf("Fasting", "Pre-Meal", "Exercise"), reading.getTagsList())
    }

    @Test
    fun `getTagsList - handles whitespace in tags`() {
        val reading = createReading(5.5, tags = "Fasting, Pre-Meal , Exercise")
        assertEquals(listOf("Fasting", "Pre-Meal", "Exercise"), reading.getTagsList())
    }

    @Test
    fun `getTagsList - null tags returns empty list`() {
        val reading = createReading(5.5, tags = null)
        assertEquals(emptyList<String>(), reading.getTagsList())
    }

    @Test
    fun `getTagsList - empty string returns empty list`() {
        val reading = createReading(5.5, tags = "")
        assertEquals(emptyList<String>(), reading.getTagsList())
    }

    @Test
    fun `getTagsList - single tag works`() {
        val reading = createReading(5.5, tags = "Bedtime")
        assertEquals(listOf("Bedtime"), reading.getTagsList())
    }

    // --- snackPass default ---

    @Test
    fun `snackPass defaults to false`() {
        val reading = createReading(5.5)
        assertFalse(reading.snackPass)
    }

    @Test
    fun `snackPass can be set to true`() {
        val reading = createReading(5.5, snackPass = true)
        assertTrue(reading.snackPass)
    }

    // --- tagsToString ---

    @Test
    fun `tagsToString - converts list to comma-separated string`() {
        val result = GlucoseReading.tagsToString(listOf("Fasting", "Pre-Meal"))
        assertEquals("Fasting,Pre-Meal", result)
    }

    @Test
    fun `tagsToString - filters blank entries`() {
        val result = GlucoseReading.tagsToString(listOf("Fasting", "", "  ", "Exercise"))
        assertEquals("Fasting,Exercise", result)
    }
}
