package com.example.currency_counter.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class Currency(
    val code: String,
    val name: String,
    val ratePerUsd: Double
)

data class RatesState(
    val currencies: List<Currency> = FALLBACK_CURRENCIES,
    // ISO date reported by the API for the freshest rate, e.g. "2026-11-17".
    val asOf: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Offline fallback rates (approximate, relative to 1 USD). Used before the first
// successful fetch and whenever the network is unavailable.
private val FALLBACK_CURRENCIES = listOf(
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

object CurrencyRepository {
    private const val RATES_URL = "https://api.frankfurter.dev/v2/rates?base=usd"
    private const val CURRENCIES_URL = "https://api.frankfurter.dev/v2/currencies"

    private val _state = MutableStateFlow(RatesState())
    val state: StateFlow<RatesState> = _state.asStateFlow()

    // Currency code -> display name, fetched once from /v2/currencies. Also acts
    // as the fiat-only filter (rates responses include crypto and metals).
    private var names: Map<String, String>? = null
    private var loadedFromDisk = false

    fun byCode(code: String): Currency? =
        _state.value.currencies.firstOrNull { it.code == code }

    fun convert(amount: Double, from: Currency, to: Currency): Double {
        return amount / from.ratePerUsd * to.ratePerUsd
    }

    // Loads cached rates/names from disk once per process, then skips the
    // network entirely when the cached rates are already from today.
    suspend fun refreshIfNeeded(context: Context) {
        if (!loadedFromDisk) {
            loadedFromDisk = true
            val snapshot = CurrencyPreferences.loadRates(context)
            if (snapshot != null) {
                names = CurrencyPreferences.loadNames(context)
                val currencies = snapshot.rates
                    .map { (code, rate) -> Currency(code, names?.get(code) ?: code, rate) }
                    .sortedBy { it.code }
                _state.value = _state.value.copy(
                    currencies = currencies,
                    asOf = snapshot.asOf
                )
            }
        }
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (_state.value.asOf != today) refresh(context)
    }

    suspend fun refresh(context: Context) {
        if (_state.value.isLoading) return
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val fetchedNames = names == null
            val nameMap = names ?: withContext(Dispatchers.IO) { fetchCurrencyNames() }
            names = nameMap
            val (rates, asOf) = withContext(Dispatchers.IO) { fetchRates(nameMap) }
            val currencies = rates
                .map { (code, rate) -> Currency(code, nameMap[code] ?: code, rate) }
                .sortedBy { it.code }
            asOf?.let { CurrencyPreferences.saveRates(context, rates, it) }
            if (fetchedNames) CurrencyPreferences.saveNames(context, nameMap)
            _state.value = RatesState(currencies = currencies, asOf = asOf)
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message ?: "Failed to update rates"
            )
        }
    }

    private fun fetchCurrencyNames(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val array = JSONArray(httpGet(CURRENCIES_URL))
        for (i in 0 until array.length()) {
            val entry = array.getJSONObject(i)
            if (entry.optString("type") == "currency") {
                result[entry.getString("iso_code")] = entry.getString("name")
            }
        }
        if (result.isEmpty()) error("Unexpected currencies response")
        return result
    }

    private fun fetchRates(nameMap: Map<String, String>): Pair<Map<String, Double>, String?> {
        val rates = mutableMapOf<String, Double>()
        var asOf: String? = null
        val array = JSONArray(httpGet(RATES_URL))
        for (i in 0 until array.length()) {
            val row = array.getJSONObject(i)
            val quote = row.getString("quote")
            if (quote !in nameMap) continue
            rates[quote] = row.getDouble("rate")
            val date = row.optString("date")
            if (date > (asOf ?: "")) asOf = date
        }
        rates["USD"] = 1.0
        return rates to asOf
    }

    private fun httpGet(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("Accept", "application/json")
            if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
