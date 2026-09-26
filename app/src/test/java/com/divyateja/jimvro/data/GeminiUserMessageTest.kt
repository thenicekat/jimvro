package com.divyateja.jimvro.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class GeminiUserMessageTest {
    @Test
    fun busyErrorsAskUserToRetry() {
        assertEquals(
            "Gemini is busy right now. Please try again in a moment.",
            geminiUserMessage(
                "This model is currently experiencing high demand. Spikes in demand are usually temporary. Please try again later.",
                503,
            ),
        )
        assertEquals(
            "Gemini is busy right now. Please try again in a moment.",
            geminiUserMessage("Resource exhausted", 429),
        )
    }

    @Test
    fun otherErrorsPassThrough() {
        val message = "API key not valid"
        assertEquals(message, geminiUserMessage(message, 400))
        assertFalse(geminiUserMessage(message, 400).contains("try again", ignoreCase = true))
    }
}
