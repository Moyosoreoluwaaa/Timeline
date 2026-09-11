package com.timeline.domain.reasoning

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PiiRedactorTest {

    @Test
    fun testEmailRedaction() {
        val input = "Contact John at john.doe@example.com for further updates."
        val expected = "Contact John at [EMAIL_REDACTED] for further updates."
        assertEquals(expected, PiiRedactor.redact(input))
        assertTrue(PiiRedactor.containsPii(input))
    }

    @Test
    fun testPhoneRedaction() {
        val input = "Call our hotline at +1-800-555-0199 or (555) 234-5678 today."
        val redacted = PiiRedactor.redact(input)
        assertFalse(redacted.contains("800-555-0199"))
        assertFalse(redacted.contains("(555) 234-5678"))
        assertTrue(redacted.contains("[PHONE_REDACTED]"))
    }

    @Test
    fun testPaymentCardRedaction() {
        val input = "Charged to card 4111-2222-3333-4444 on file."
        val expected = "Charged to card [PAYMENT_REDACTED] on file."
        assertEquals(expected, PiiRedactor.redact(input))
        assertTrue(PiiRedactor.containsPii(input))
    }

    @Test
    fun testTokenRedaction() {
        val input = "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9 and api_key: AIzaSyD3fakeAPIkey4210984321456789012"
        val redacted = PiiRedactor.redact(input)
        assertFalse(redacted.contains("Bearer eyJhbGci"))
        assertFalse(redacted.contains("AIzaSyD3fakeAPIkey"))
        assertTrue(redacted.contains("[TOKEN_REDACTED]"))
    }

    @Test
    fun testNormalContentPreserved() {
        val input = "Project sprint review at 10:30 AM with 4 developers. Progress is 85%."
        val redacted = PiiRedactor.redact(input)
        assertEquals(input, redacted)
        assertFalse(PiiRedactor.containsPii(input))
    }
}
