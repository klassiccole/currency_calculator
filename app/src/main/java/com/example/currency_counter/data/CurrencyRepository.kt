package com.example.currency_counter.data

data class Currency(
    val code: String,
    val name: String,
    val ratePerUsd: Double
)

// Offline static rates (approximate, relative to 1 USD).
// Future: replace with live rates fetched from an API (e.g., Frankfurter).
object CurrencyRepository {
    val all: List<Currency> = listOf(
        Currency("USD", "US Dollar", 1.0),
        Currency("EUR", "Euro", 0.92),
        Currency("GBP", "British Pound", 0.79),
        Currency("CAD", "Canadian Dollar", 1.36),
        Currency("MXN", "Mexican Peso", 17.10),
        Currency("JPY", "Japanese Yen", 149.0),
        Currency("CHF", "Swiss Franc", 0.88),
        Currency("AUD", "Australian Dollar", 1.52),
        Currency("CNY", "Chinese Yuan", 7.24),
        Currency("INR", "Indian Rupee", 83.0)
    )

    private val byCode: Map<String, Currency> = all.associateBy { it.code }

    fun byCode(code: String): Currency = byCode.getValue(code)

    fun convert(amount: Double, from: Currency, to: Currency): Double {
        return amount / from.ratePerUsd * to.ratePerUsd
    }
}
