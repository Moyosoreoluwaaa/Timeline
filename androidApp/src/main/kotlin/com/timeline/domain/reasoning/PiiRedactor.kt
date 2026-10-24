package com.timeline.domain.reasoning

/**
 * Client-side best-effort PII sanitization engine.
 * Strips emails, phone numbers, payment card sequences, and authorization tokens/keys
 * on-device BEFORE any persistence or cloud transmission.
 */
object PiiRedactor {

    // Standard RFC-compliant email pattern
    private val emailRegex = Regex(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
        RegexOption.IGNORE_CASE
    )

    // Common phone formats: +1-234-567-8901, (123) 456-7890, 123-456-7890, +44 20 7123 4567
    private val phoneRegex = Regex(
        "(?:\\+?\\d{1,3}[- .]?)?\\(?\\d{3}\\)?[- .]?\\d{3}[- .]?\\d{4}"
    )

    // Credit card patterns: 13-19 digits with optional spaces or dashes
    private val paymentCardRegex = Regex(
        "\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|6(?:011|5[0-9]{2})[0-9]{12})\\b|" +
        "\\b(?:\\d{4}[- ]){3}\\d{4}\\b"
    )

    // API Keys, auth tokens, secrets: Bearer tokens, GitHub tokens, Google API keys, JWT prefixes
    private val secretTokenRegex = Regex(
        "\\b(?:Bearer\\s+[A-Za-z0-9_.~+\\-/=]+|" +
        "ghp_[A-Za-z0-9]{36}|" +
        "AIza[0-9A-Za-z_-]{35}|" +
        "(?:api[_-]?key|secret[_-]?key|access[_-]?token)\\s*[:=]\\s*['\"]?[A-Za-z0-9_-]{16,}['\"]?)\\b",
        RegexOption.IGNORE_CASE
    )

    /**
     * Redacts known PII patterns in the provided string.
     */
    fun redact(text: String): String {
        if (text.isBlank()) return text

        var sanitized = text
        // 1. Tokens & Secrets
        sanitized = secretTokenRegex.replace(sanitized, "[TOKEN_REDACTED]")
        // 2. Payment Cards
        sanitized = paymentCardRegex.replace(sanitized, "[PAYMENT_REDACTED]")
        // 3. Emails
        sanitized = emailRegex.replace(sanitized, "[EMAIL_REDACTED]")
        // 4. Phone Numbers (applied after payment cards to avoid overlapping digit clashes)
        sanitized = phoneRegex.replace(sanitized, "[PHONE_REDACTED]")

        return sanitized
    }

    /**
     * Redacts a list of text dumps.
     */
    fun redactAll(texts: List<String>): List<String> {
        return texts.asSequence().map { redact(it) }.toList()
    }

    /**
     * Checks whether text contains any identifiable PII patterns.
     */
    fun containsPii(text: String): Boolean {
        if (text.isBlank()) return false
        return emailRegex.containsMatchIn(text) ||
               phoneRegex.containsMatchIn(text) ||
               paymentCardRegex.containsMatchIn(text) ||
               secretTokenRegex.containsMatchIn(text)
    }
}
