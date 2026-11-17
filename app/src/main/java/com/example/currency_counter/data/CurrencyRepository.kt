package com.example.currency_counter.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Currency(
    val code: String,
    val name: String,
    val ratePerUsd: Double
)

data class MetalPrice(
    val code: String,
    val name: String,
    val pricePerOz: Double,
    val pricePerGram: Double
)

data class ProviderMeta(
    val key: String,
    val name: String,
    val country: String?
)

data class ProviderQuote(
    val name: String,
    val country: String?,
    val pricePerOz: Double,
    val pricePerGram: Double,
    val asOf: String,
    val differsFromBlend: Boolean = false
)

data class MetalDetailState(
    val isLoading: Boolean = false,
    val quotes: List<ProviderQuote> = emptyList(),
    val failedCount: Int = 0,
    val error: String? = null
)

private const val TROY_OUNCE_GRAMS = 31.1034768

private val METAL_CODES = listOf("XAU", "XAG", "XPT", "XPD")

private val METAL_NAMES = mapOf(
    "XAU" to "Gold",
    "XAG" to "Silver",
    "XPT" to "Platinum",
    "XPD" to "Palladium"
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
    Currency("INR", "Indian Rupee", 83.0),
    Currency("XAU", "Gold (Troy Ounce)", 0.00025677),
    Currency("XAG", "Silver (Troy Ounce)", 0.02026),
    Currency("XPT", "Platinum (Troy Ounce)", 0.0006625),
    Currency("XPD", "Palladium (Troy Ounce)", 0.0009784)
)

object CurrencyRepository {
    private const val RATES_URL = "https://api.frankfurter.dev/v2/rates?base=usd"
    private const val CURRENCIES_URL = "https://api.frankfurter.dev/v2/currencies"
    private const val CURRENCY_URL = "https://api.frankfurter.dev/v2/currency"
    private const val PROVIDERS_URL = "https://api.frankfurter.dev/v2/providers"

    private val _state = MutableStateFlow(RatesState())
    val state: StateFlow<RatesState> = _state.asStateFlow()

    private val _metalDetail = MutableStateFlow(MetalDetailState())
    val metalDetail: StateFlow<MetalDetailState> = _metalDetail.asStateFlow()

    private var providerMeta: Map<String, ProviderMeta>? = null
    private val metalProviderKeys = mutableMapOf<String, List<String>>()

    // Currency code -> display name, fetched once from /v2/currencies. Also acts
    // as the fiat-only filter (rates responses include crypto and metals).
    private var names: Map<String, String>? = null
    private var loadedFromDisk = false

    fun byCode(code: String): Currency? =
        _state.value.currencies.firstOrNull { it.code == code }

    fun convert(amount: Double, from: Currency, to: Currency): Double {
        return amount / from.ratePerUsd * to.ratePerUsd
    }

    // Blended precious-metal prices in USD, in fixed display order.
    fun metals(): List<MetalPrice> {
        val byCode = _state.value.currencies.associateBy { it.code }
        return METAL_CODES.mapNotNull { code ->
            byCode[code]?.let {
                val perOz = 1.0 / it.ratePerUsd
                MetalPrice(
                    code = code,
                    name = METAL_NAMES.getValue(code),
                    pricePerOz = perOz,
                    pricePerGram = perOz / TROY_OUNCE_GRAMS
                )
            }
        }
    }

    fun metal(code: String): MetalPrice? = metals().firstOrNull { it.code == code }

    // Loads cached institute metadata, then fetches each institute's own rate
    // for the metal in parallel. Institutes are cached to disk per metal.
    suspend fun loadMetalDetail(code: String, context: Context) {
        if (_metalDetail.value.isLoading) return
        _metalDetail.value = MetalDetailState(isLoading = true)
        try {
            val meta = providerMeta ?: (CurrencyPreferences.loadProviders(context) ?: run {
                val fetched = withContext(Dispatchers.IO) { fetchProviderMeta() }
                CurrencyPreferences.saveProviders(context, fetched)
                fetched
            })
            providerMeta = meta
            val keys = metalProviderKeys.getOrPut(code) {
                CurrencyPreferences.loadMetalProviders(context, code) ?: run {
                    val fetched = withContext(Dispatchers.IO) { fetchMetalProviders(code) }
                    CurrencyPreferences.saveMetalProviders(context, code, fetched)
                    fetched
                }
            }
            val quotes = coroutineScope {
                keys.map { key ->
                    async(Dispatchers.IO) {
                        runCatching { fetchProviderQuote(key, code, meta[key]) }.getOrNull()
                    }
                }.awaitAll().filterNotNull()
            }
            // Some institutes report gold in inconsistent units (e.g. BNA's
            // Angola kwanza feed). Flag any row far from the median so the raw
            // data stays visible without looking like a bug.
            val median = quotes.map { it.pricePerOz }.sorted()
                .takeIf { it.isNotEmpty() }?.let {
                    if (it.size % 2 == 1) it[it.size / 2]
                    else (it[it.size / 2 - 1] + it[it.size / 2]) / 2.0
                } ?: 0.0
            val flagged = quotes.map { quote ->
                if (median > 0 && Math.abs(quote.pricePerOz - median) / median > 0.25) {
                    quote.copy(differsFromBlend = true)
                } else {
                    quote
                }
            }
            _metalDetail.value = MetalDetailState(
                isLoading = false,
                quotes = flagged.sortedBy { it.name },
                failedCount = keys.size - quotes.size
            )
        } catch (e: Exception) {
            _metalDetail.value = MetalDetailState(error = e.message ?: "Failed to load institutes")
        }
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
            if (entry.optString("type") == "currency" || entry.optString("type") == "metal") {
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

    private fun fetchProviderMeta(): Map<String, ProviderMeta> {
        val result = mutableMapOf<String, ProviderMeta>()
        val array = JSONArray(httpGet(PROVIDERS_URL))
        for (i in 0 until array.length()) {
            val entry = array.getJSONObject(i)
            result[entry.getString("key")] = ProviderMeta(
                key = entry.getString("key"),
                name = entry.getString("name"),
                country = entry.optString("country_code").ifEmpty { null }
            )
        }
        if (result.isEmpty()) error("Unexpected providers response")
        return result
    }

    private fun fetchMetalProviders(code: String): List<String> {
        val entry = JSONObject(httpGet("$CURRENCY_URL/$code"))
        val providers = entry.optJSONArray("providers") ?: JSONArray()
        val keys = mutableListOf<String>()
        for (i in 0 until providers.length()) keys.add(providers.getString(i))
        if (keys.isEmpty()) error("No reporting institutes for $code")
        return keys
    }

    private fun fetchProviderQuote(
        key: String,
        code: String,
        meta: ProviderMeta?
    ): ProviderQuote {
        val array = JSONArray(httpGet("$PROVIDERS_URL/$key/rates?base=usd&quotes=$code"))
        if (array.length() == 0) error("No rate from $key")
        val row = array.getJSONObject(0)
        val perOz = 1.0 / row.getDouble("rate")
        return ProviderQuote(
            name = meta?.name ?: key,
            country = meta?.country,
            pricePerOz = perOz,
            pricePerGram = perOz / TROY_OUNCE_GRAMS,
            asOf = row.optString("date")
        )
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
