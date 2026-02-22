package org.kulus.android.data.api.v3

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PhoneValidatorTest {

    @Test
    fun `validate returns valid for US number`() {
        val result = PhoneValidator.validate("(646) 484-9595")
        assertThat(result).isInstanceOf(PhoneValidator.Result.Valid::class.java)
        assertThat((result as PhoneValidator.Result.Valid).e164Number).isEqualTo("+16464849595")
    }

    @Test
    fun `validate returns valid for number with country code`() {
        val result = PhoneValidator.validate("+1 646 484 9595")
        assertThat(result).isInstanceOf(PhoneValidator.Result.Valid::class.java)
        assertThat((result as PhoneValidator.Result.Valid).e164Number).isEqualTo("+16464849595")
    }

    @Test
    fun `validate returns valid for plain digits`() {
        val result = PhoneValidator.validate("6464849595")
        assertThat(result).isInstanceOf(PhoneValidator.Result.Valid::class.java)
        assertThat((result as PhoneValidator.Result.Valid).e164Number).isEqualTo("+16464849595")
    }

    @Test
    fun `validate returns valid for 11 digit US number`() {
        val result = PhoneValidator.validate("16464849595")
        assertThat(result).isInstanceOf(PhoneValidator.Result.Valid::class.java)
        assertThat((result as PhoneValidator.Result.Valid).e164Number).isEqualTo("+16464849595")
    }

    @Test
    fun `validate returns invalid for empty string`() {
        val result = PhoneValidator.validate("")
        assertThat(result).isInstanceOf(PhoneValidator.Result.Invalid::class.java)
        assertThat((result as PhoneValidator.Result.Invalid).reason).contains("empty")
    }

    @Test
    fun `validate returns invalid for blank string`() {
        val result = PhoneValidator.validate("   ")
        assertThat(result).isInstanceOf(PhoneValidator.Result.Invalid::class.java)
    }

    @Test
    fun `validate returns invalid for too short number`() {
        val result = PhoneValidator.validate("123")
        assertThat(result).isInstanceOf(PhoneValidator.Result.Invalid::class.java)
    }

    @Test
    fun `isE164Format returns true for valid E164`() {
        assertThat(PhoneValidator.isE164Format("+16464849595")).isTrue()
        assertThat(PhoneValidator.isE164Format("+442071234567")).isTrue()
    }

    @Test
    fun `isE164Format returns false for non-E164`() {
        assertThat(PhoneValidator.isE164Format("6464849595")).isFalse()
        assertThat(PhoneValidator.isE164Format("(646) 484-9595")).isFalse()
        assertThat(PhoneValidator.isE164Format("+0123456")).isFalse() // Can't start with 0
    }

    @Test
    fun `normalizeOrThrow returns E164 for valid number`() {
        val result = PhoneValidator.normalizeOrThrow("(646) 484-9595")
        assertThat(result).isEqualTo("+16464849595")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `normalizeOrThrow throws for invalid number`() {
        PhoneValidator.normalizeOrThrow("")
    }

    @Test
    fun `normalizeOrNull returns E164 for valid number`() {
        val result = PhoneValidator.normalizeOrNull("(646) 484-9595")
        assertThat(result).isEqualTo("+16464849595")
    }

    @Test
    fun `normalizeOrNull returns null for invalid number`() {
        assertThat(PhoneValidator.normalizeOrNull("")).isNull()
        assertThat(PhoneValidator.normalizeOrNull("123")).isNull()
    }
}
