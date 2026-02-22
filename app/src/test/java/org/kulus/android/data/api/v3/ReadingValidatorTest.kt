package org.kulus.android.data.api.v3

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.Test

class ReadingValidatorTest {

    @Test
    fun `MIN_MMOL is 0_1`() {
        assertThat(ReadingValidator.MIN_MMOL).isEqualTo(0.1)
    }

    @Test
    fun `MAX_MMOL is 50_0`() {
        assertThat(ReadingValidator.MAX_MMOL).isEqualTo(50.0)
    }

    @Test
    fun `MIN_MGDL is derived from MIN_MMOL`() {
        assertThat(ReadingValidator.MIN_MGDL).isCloseTo(1.80182, within(0.001))
    }

    @Test
    fun `MAX_MGDL is derived from MAX_MMOL`() {
        assertThat(ReadingValidator.MAX_MGDL).isCloseTo(900.91, within(0.01))
    }

    @Test
    fun `validate returns valid for normal mmol reading`() {
        val result = ReadingValidator.validate(5.5, GlucoseUnits.MMOL_L)
        assertThat(result).isInstanceOf(ReadingValidator.Result.Valid::class.java)
        assertThat((result as ReadingValidator.Result.Valid).value).isEqualTo(5.5)
        assertThat(result.units).isEqualTo(GlucoseUnits.MMOL_L)
    }

    @Test
    fun `validate returns valid for normal mgdl reading`() {
        val result = ReadingValidator.validate(100.0, GlucoseUnits.MG_DL)
        assertThat(result).isInstanceOf(ReadingValidator.Result.Valid::class.java)
        assertThat((result as ReadingValidator.Result.Valid).value).isEqualTo(100.0)
        assertThat(result.units).isEqualTo(GlucoseUnits.MG_DL)
    }

    @Test
    fun `validate returns valid for minimum value`() {
        val result = ReadingValidator.validate(0.1, GlucoseUnits.MMOL_L)
        assertThat(result).isInstanceOf(ReadingValidator.Result.Valid::class.java)
    }

    @Test
    fun `validate returns valid for maximum value`() {
        val result = ReadingValidator.validate(50.0, GlucoseUnits.MMOL_L)
        assertThat(result).isInstanceOf(ReadingValidator.Result.Valid::class.java)
    }

    @Test
    fun `validate returns invalid for value below minimum`() {
        val result = ReadingValidator.validate(0.05, GlucoseUnits.MMOL_L)
        assertThat(result).isInstanceOf(ReadingValidator.Result.Invalid::class.java)
        assertThat((result as ReadingValidator.Result.Invalid).reason).contains("too low")
    }

    @Test
    fun `validate returns invalid for value above maximum`() {
        val result = ReadingValidator.validate(55.0, GlucoseUnits.MMOL_L)
        assertThat(result).isInstanceOf(ReadingValidator.Result.Invalid::class.java)
        assertThat((result as ReadingValidator.Result.Invalid).reason).contains("too high")
    }

    @Test
    fun `validate returns invalid for NaN`() {
        val result = ReadingValidator.validate(Double.NaN, GlucoseUnits.MMOL_L)
        assertThat(result).isInstanceOf(ReadingValidator.Result.Invalid::class.java)
        assertThat((result as ReadingValidator.Result.Invalid).reason).contains("valid number")
    }

    @Test
    fun `validate returns invalid for Infinity`() {
        val result = ReadingValidator.validate(Double.POSITIVE_INFINITY, GlucoseUnits.MMOL_L)
        assertThat(result).isInstanceOf(ReadingValidator.Result.Invalid::class.java)
    }

    @Test
    fun `validateOrThrow returns value for valid reading`() {
        val result = ReadingValidator.validateOrThrow(5.5, GlucoseUnits.MMOL_L)
        assertThat(result).isEqualTo(5.5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `validateOrThrow throws for invalid reading`() {
        ReadingValidator.validateOrThrow(100.0, GlucoseUnits.MMOL_L) // Too high
    }

    @Test
    fun `isDangerous returns true for hypo`() {
        assertThat(ReadingValidator.isDangerous(2.5)).isTrue()
        assertThat(ReadingValidator.isDangerous(2.9)).isTrue()
    }

    @Test
    fun `isDangerous returns true for severe hyper`() {
        assertThat(ReadingValidator.isDangerous(20.1)).isTrue()
        assertThat(ReadingValidator.isDangerous(25.0)).isTrue()
    }

    @Test
    fun `isDangerous returns false for normal values`() {
        assertThat(ReadingValidator.isDangerous(3.0)).isFalse()
        assertThat(ReadingValidator.isDangerous(5.5)).isFalse()
        assertThat(ReadingValidator.isDangerous(10.0)).isFalse()
        assertThat(ReadingValidator.isDangerous(20.0)).isFalse()
    }

    @Test
    fun `validate converts mgdl to mmol for range check`() {
        // 1 mg/dL is about 0.055 mmol/L - below minimum
        val result = ReadingValidator.validate(1.0, GlucoseUnits.MG_DL)
        assertThat(result).isInstanceOf(ReadingValidator.Result.Invalid::class.java)

        // 1000 mg/dL is about 55.5 mmol/L - above maximum
        val result2 = ReadingValidator.validate(1000.0, GlucoseUnits.MG_DL)
        assertThat(result2).isInstanceOf(ReadingValidator.Result.Invalid::class.java)

        // 100 mg/dL is about 5.5 mmol/L - valid
        val result3 = ReadingValidator.validate(100.0, GlucoseUnits.MG_DL)
        assertThat(result3).isInstanceOf(ReadingValidator.Result.Valid::class.java)
    }
}
