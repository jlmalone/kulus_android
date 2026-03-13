package org.kulus.android.data.api.v3

import org.junit.Assert.*
import org.junit.Test

class PhoneValidatorTest {

    // --- isE164Format ---

    @Test
    fun `isE164Format - valid E164 number`() {
        assertTrue(PhoneValidator.isE164Format("+15109842762"))
    }

    @Test
    fun `isE164Format - valid short international number`() {
        assertTrue(PhoneValidator.isE164Format("+44123456"))
    }

    @Test
    fun `isE164Format - no plus sign is invalid`() {
        assertFalse(PhoneValidator.isE164Format("15109842762"))
    }

    @Test
    fun `isE164Format - plus only is invalid`() {
        assertFalse(PhoneValidator.isE164Format("+"))
    }

    @Test
    fun `isE164Format - starts with plus zero is invalid`() {
        assertFalse(PhoneValidator.isE164Format("+0123456789"))
    }

    @Test
    fun `isE164Format - too short is invalid`() {
        assertFalse(PhoneValidator.isE164Format("+1"))
    }

    @Test
    fun `isE164Format - letters are invalid`() {
        assertFalse(PhoneValidator.isE164Format("+1510abc4762"))
    }

    @Test
    fun `isE164Format - empty string is invalid`() {
        assertFalse(PhoneValidator.isE164Format(""))
    }

    @Test
    fun `isE164Format - spaces are invalid`() {
        assertFalse(PhoneValidator.isE164Format("+1 510 984 2762"))
    }

    @Test
    fun `isE164Format - too long is invalid`() {
        // E.164 max is 15 digits after +
        assertFalse(PhoneValidator.isE164Format("+1234567890123456"))
    }

    // --- validate ---

    @Test
    fun `validate - valid US number returns Valid`() {
        val result = PhoneValidator.validate("+15109842762")
        assertTrue(result is PhoneValidator.Result.Valid)
        assertEquals("+15109842762", (result as PhoneValidator.Result.Valid).e164Number)
    }

    @Test
    fun `validate - blank input returns Invalid`() {
        val result = PhoneValidator.validate("")
        assertTrue(result is PhoneValidator.Result.Invalid)
    }

    @Test
    fun `validate - whitespace only returns Invalid`() {
        val result = PhoneValidator.validate("   ")
        assertTrue(result is PhoneValidator.Result.Invalid)
    }
}
