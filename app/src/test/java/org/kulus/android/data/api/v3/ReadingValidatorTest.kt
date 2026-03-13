package org.kulus.android.data.api.v3

import org.junit.Assert.*
import org.junit.Test

class ReadingValidatorTest {

    // --- Valid range ---

    @Test
    fun `validate - normal mmol value is valid`() {
        val result = ReadingValidator.validate(5.5, GlucoseUnits.MMOL_L)
        assertTrue(result is ReadingValidator.Result.Valid)
    }

    @Test
    fun `validate - min boundary 0_1 mmol is valid`() {
        val result = ReadingValidator.validate(0.1, GlucoseUnits.MMOL_L)
        assertTrue(result is ReadingValidator.Result.Valid)
    }

    @Test
    fun `validate - max boundary 50 mmol is valid`() {
        val result = ReadingValidator.validate(50.0, GlucoseUnits.MMOL_L)
        assertTrue(result is ReadingValidator.Result.Valid)
    }

    @Test
    fun `validate - normal mgdl value is valid`() {
        val result = ReadingValidator.validate(100.0, GlucoseUnits.MG_DL)
        assertTrue(result is ReadingValidator.Result.Valid)
    }

    // --- Out of range ---

    @Test
    fun `validate - zero mmol is invalid`() {
        val result = ReadingValidator.validate(0.0, GlucoseUnits.MMOL_L)
        assertTrue(result is ReadingValidator.Result.Invalid)
    }

    @Test
    fun `validate - negative value is invalid`() {
        val result = ReadingValidator.validate(-1.0, GlucoseUnits.MMOL_L)
        assertTrue(result is ReadingValidator.Result.Invalid)
    }

    @Test
    fun `validate - above max mmol is invalid`() {
        val result = ReadingValidator.validate(50.1, GlucoseUnits.MMOL_L)
        assertTrue(result is ReadingValidator.Result.Invalid)
    }

    @Test
    fun `validate - NaN is invalid`() {
        val result = ReadingValidator.validate(Double.NaN, GlucoseUnits.MMOL_L)
        assertTrue(result is ReadingValidator.Result.Invalid)
    }

    @Test
    fun `validate - Infinity is invalid`() {
        val result = ReadingValidator.validate(Double.POSITIVE_INFINITY, GlucoseUnits.MMOL_L)
        assertTrue(result is ReadingValidator.Result.Invalid)
    }

    // --- isDangerous ---

    @Test
    fun `isDangerous - hypo below 3_0 is dangerous`() {
        assertTrue(ReadingValidator.isDangerous(2.5))
    }

    @Test
    fun `isDangerous - severe hyper above 20 is dangerous`() {
        assertTrue(ReadingValidator.isDangerous(25.0))
    }

    @Test
    fun `isDangerous - normal value is not dangerous`() {
        assertFalse(ReadingValidator.isDangerous(5.5))
    }

    @Test
    fun `isDangerous - exactly 3_0 is not dangerous`() {
        assertFalse(ReadingValidator.isDangerous(3.0))
    }

    @Test
    fun `isDangerous - exactly 20_0 is not dangerous`() {
        assertFalse(ReadingValidator.isDangerous(20.0))
    }
}
