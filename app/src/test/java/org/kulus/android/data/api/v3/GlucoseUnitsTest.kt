package org.kulus.android.data.api.v3

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.Test

class GlucoseUnitsTest {

    @Test
    fun `conversion factor is 18_0182`() {
        assertThat(GlucoseUnits.CONVERSION_FACTOR).isEqualTo(18.0182)
    }

    @Test
    fun `mmolToMgdl converts correctly`() {
        assertThat(GlucoseUnits.mmolToMgdl(5.5)).isCloseTo(99.1001, within(0.001))
        assertThat(GlucoseUnits.mmolToMgdl(1.0)).isCloseTo(18.0182, within(0.001))
        assertThat(GlucoseUnits.mmolToMgdl(10.0)).isCloseTo(180.182, within(0.001))
    }

    @Test
    fun `mgdlToMmol converts correctly`() {
        assertThat(GlucoseUnits.mgdlToMmol(100.0)).isCloseTo(5.55, within(0.01))
        assertThat(GlucoseUnits.mgdlToMmol(180.0)).isCloseTo(9.99, within(0.01))
        assertThat(GlucoseUnits.mgdlToMmol(18.0182)).isCloseTo(1.0, within(0.001))
    }

    @Test
    fun `roundtrip conversion preserves value`() {
        val original = 6.5
        val mgdl = GlucoseUnits.mmolToMgdl(original)
        val backToMmol = GlucoseUnits.mgdlToMmol(mgdl)
        assertThat(backToMmol).isCloseTo(original, within(0.0001))
    }

    @Test
    fun `fromString parses case insensitively`() {
        assertThat(GlucoseUnits.fromString("mmol/L")).isEqualTo(GlucoseUnits.MMOL_L)
        assertThat(GlucoseUnits.fromString("MMOL/L")).isEqualTo(GlucoseUnits.MMOL_L)
        assertThat(GlucoseUnits.fromString("mmol")).isEqualTo(GlucoseUnits.MMOL_L)
        assertThat(GlucoseUnits.fromString("mg/dL")).isEqualTo(GlucoseUnits.MG_DL)
        assertThat(GlucoseUnits.fromString("MG/DL")).isEqualTo(GlucoseUnits.MG_DL)
        assertThat(GlucoseUnits.fromString("mgdl")).isEqualTo(GlucoseUnits.MG_DL)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `fromString throws for unknown unit`() {
        GlucoseUnits.fromString("unknown")
    }

    @Test
    fun `fromStringOrNull returns null for unknown unit`() {
        assertThat(GlucoseUnits.fromStringOrNull("unknown")).isNull()
        assertThat(GlucoseUnits.fromStringOrNull("")).isNull()
    }

    @Test
    fun `apiValue returns correct string`() {
        assertThat(GlucoseUnits.MMOL_L.apiValue).isEqualTo("mmol/L")
        assertThat(GlucoseUnits.MG_DL.apiValue).isEqualTo("mg/dL")
    }
}
