package com.example.currency_counter.ui

import java.util.Locale

// Three tiers: 2dp with grouping for normal magnitudes, 4dp for small values,
// 6dp for tiny ones (e.g. metal rates quoted in troy ounces per USD).
internal fun formatAmount(value: Double): String {
    val pattern = when {
        value >= 1.0 || value <= -1.0 -> "%,.2f"
        value >= 0.01 || value <= -0.01 -> "%.4f"
        else -> "%.6f"
    }
    return String.format(Locale.US, pattern, value)
}

// Typed text may contain the thousands separators we render into derived values;
// grouping is display-only, so strip it before parsing.
internal fun parseAmount(text: String): Double? =
    text.replace(",", "").toDoubleOrNull()
