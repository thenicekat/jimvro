package com.divyateja.jimvro.ui

/** Accept decimal separators used by Android keyboards, but never drop invalid input silently. */
internal fun decimalValue(value: String): Double? =
    value.trim().replace(',', '.').toDoubleOrNull()?.takeIf(Double::isFinite)

internal fun numberInputError(value: String, allowZero: Boolean = true, maximum: Double? = null): String? {
    if (value.isBlank()) return null
    val number = decimalValue(value) ?: return "Enter a valid number"
    if (number < 0 || (!allowZero && number == 0.0)) {
        return if (allowZero) "Use 0 or more" else "Use a value above 0"
    }
    if (maximum != null && number > maximum) return "Use ${maximum.toInt()} or less"
    return null
}
