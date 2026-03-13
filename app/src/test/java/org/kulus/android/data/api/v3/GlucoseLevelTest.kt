package org.kulus.android.data.api.v3

import org.junit.Assert.*
import org.junit.Test

class GlucoseLevelTest {

    // --- classify ---

    @Test
    fun `classify - hypo below 3_9 is PURPLE`() {
        assertEquals(GlucoseLevel.PURPLE, GlucoseLevel.classify(3.0))
    }

    @Test
    fun `classify - exactly 3_9 is GREEN`() {
        assertEquals(GlucoseLevel.GREEN, GlucoseLevel.classify(3.9))
    }

    @Test
    fun `classify - normal 5_5 is GREEN`() {
        assertEquals(GlucoseLevel.GREEN, GlucoseLevel.classify(5.5))
    }

    @Test
    fun `classify - exactly 7_8 is GREEN`() {
        assertEquals(GlucoseLevel.GREEN, GlucoseLevel.classify(7.8))
    }

    @Test
    fun `classify - elevated 9_0 is ORANGE`() {
        assertEquals(GlucoseLevel.ORANGE, GlucoseLevel.classify(9.0))
    }

    @Test
    fun `classify - exactly 11_1 is ORANGE`() {
        assertEquals(GlucoseLevel.ORANGE, GlucoseLevel.classify(11.1))
    }

    @Test
    fun `classify - high 15_0 is RED`() {
        assertEquals(GlucoseLevel.RED, GlucoseLevel.classify(15.0))
    }

    @Test
    fun `classify - very high 25_0 is RED`() {
        assertEquals(GlucoseLevel.RED, GlucoseLevel.classify(25.0))
    }

    @Test
    fun `classify - zero is PURPLE`() {
        assertEquals(GlucoseLevel.PURPLE, GlucoseLevel.classify(0.0))
    }

    @Test
    fun `classify - negative is PURPLE`() {
        assertEquals(GlucoseLevel.PURPLE, GlucoseLevel.classify(-1.0))
    }

    // --- fromString ---

    @Test
    fun `fromString - green`() {
        assertEquals(GlucoseLevel.GREEN, GlucoseLevel.fromString("green"))
    }

    @Test
    fun `fromString - Green mixed case`() {
        assertEquals(GlucoseLevel.GREEN, GlucoseLevel.fromString("Green"))
    }

    @Test
    fun `fromString - orange`() {
        assertEquals(GlucoseLevel.ORANGE, GlucoseLevel.fromString("orange"))
    }

    @Test
    fun `fromString - red`() {
        assertEquals(GlucoseLevel.RED, GlucoseLevel.fromString("red"))
    }

    @Test
    fun `fromString - purple`() {
        assertEquals(GlucoseLevel.PURPLE, GlucoseLevel.fromString("purple"))
    }

    @Test
    fun `fromString - unknown returns null`() {
        assertNull(GlucoseLevel.fromString("unknown"))
    }

    // --- displayName and description ---

    @Test
    fun `GREEN has correct displayName`() {
        assertEquals("Green", GlucoseLevel.GREEN.displayName)
    }

    @Test
    fun `PURPLE has correct description`() {
        assertEquals("Critical hypoglycemia", GlucoseLevel.PURPLE.description)
    }

    @Test
    fun `RED has correct description`() {
        assertEquals("High - needs attention", GlucoseLevel.RED.description)
    }
}
