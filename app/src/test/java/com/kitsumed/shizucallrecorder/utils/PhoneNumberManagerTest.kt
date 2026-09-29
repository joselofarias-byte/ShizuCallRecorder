package com.kitsumed.shizucallrecorder.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberManagerTest {

    @Test
    fun normaliseKeepsPlusAndDigits() {
        assertEquals("+33612345678", PhoneNumberManager.normalisePhoneNumber(" +33 6 12 34 56 78 "))
    }

    @Test
    fun normaliseStripsNonDigitsWithoutPlus() {
        assertEquals("0912345678", PhoneNumberManager.normalisePhoneNumber("(09) 1234-5678"))
    }

    @Test
    fun normaliseTreatsAnonymousTokensAsBlank() {
        listOf("unknown", "PRIVATE", "anonymous", "+anonymous", "+", "#", " Unknown ").forEach { token ->
            assertEquals("anonymous token: $token", "", PhoneNumberManager.normalisePhoneNumber(token))
        }
    }

    @Test
    fun normaliseBlankStaysBlank() {
        assertEquals("", PhoneNumberManager.normalisePhoneNumber("   "))
    }
}
