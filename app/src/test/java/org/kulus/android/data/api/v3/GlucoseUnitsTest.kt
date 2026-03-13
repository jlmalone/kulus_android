package org.kulus.android.data.api.v3

import org.junit.Assert.*
import org.junit.Test

class GlucoseUnitsTest {

    // --- mmolToMgdl ---

    @Test
    fun `mmolToMgdl - 5_5 mmol converts to approximately 99_1 mgdl`() {
        val result = GlucoseUnits.mmolToMgdl(5.5)
        assertEquals(99.1, result, 0.1)
    }

    @Test
    fun `mmolToMgdl - zero converts to zero`() {
        assertEquals(0.0, GlucoseUnits.mmolToMgdl(0.0), 0.001)
    }

    @Test
    fun `mmolToMgdl - 10 mmol converts to approximately 180 mgdl`() {
        val result = GlucoseUnits.mmolToMgdl(10.0)
        assertEquals(180.2, result, 0.1)
    }

    // --- mgdlToMmol ---

    @Test
    fun `mgdlToMmol - 100 mgdl converts to approximately 5_55 mmol`() {
        val result = GlucoseUnits.mgdlToMmol(100.0)
        assertEquals(5.55, result, 0.01)
    }

    @Test
    fun `mgdlToMmol - zero converts to zero`() {
        assertEquals(0.0, GlucoseUnits.mgdlToMmol(0.0), 0.001)
    }

    @Test
    fun `mgdlToMmol - 180 mgdl converts to approximately 10 mmol`() {
        val result = GlucoseUnits.mgdlToMmol(180.0)
        assertEquals(9.99, result, 0.01)
    }

    // --- Round-trip conversion ---

    @Test
    fun `round-trip mmol to mgdl to mmol preserves value`() {
        val original = 7.5
        val mgdl = GlucoseUnits.mmolToMgdl(original)
        val backToMmol = GlucoseUnits.mgdlToMmol(mgdl)
        assertEquals(original, backToMmol, 0.001)
    }

    // --- CONVERSION_FACTOR ---

    @Test
    fun `conversion factor is 18_0182`() {
        assertEquals(18.0182, GlucoseUnits.CONVERSION_FACTOR, 0.0001)
    }

    // --- fromString ---

    @Test
    fun `fromString - mmol_L`() {
        assertEquals(GlucoseUnits.MMOL_L, GlucoseUnits.fromString("mmol/L"))
    }

    @Test
    fun `fromString - mmol lowercase`() {
        assertEquals(GlucoseUnits.MMOL_L, GlucoseUnits.fromString("mmol"))
    }

    @Test
    fun `fromString - mgdl`() {
        assertEquals(GlucoseUnits.MG_DL, GlucoseUnits.fromString("mg/dL"))
    }

    @Test
    fun `fromString - mgdl lowercase`() {
        assertEquals(GlucoseUnits.MG_DL, GlucoseUnits.fromString("mgdl"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromString - unknown throws exception`() {
        GlucoseUnits.fromString("unknown")
    }

    // --- fromStringOrNull ---

    @Test
    fun `fromStringOrNull - valid returns enum`() {
        assertEquals(GlucoseUnits.MMOL_L, GlucoseUnits.fromStringOrNull("mmol/L"))
    }

    @Test
    fun `fromStringOrNull - unknown returns null`() {
        assertNull(GlucoseUnits.fromStringOrNull("unknown"))
    }
}
