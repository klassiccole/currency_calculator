package com.example.currency_counter.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// Small SharedPreferences-backed cache: last successful rates + currency names,
// so the app shows real last-known rates on offline cold starts instead of the
// hardcoded fallback list.
object CurrencyPreferences {
    private const val FILE = "currency_prefs"
    // _v2: metals joined the cached rate set; versioned keys invalidate the
    // pre-metals cache written by older builds.
    private const val KEY_RATES = "rates_v2"
    private const val KEY_NAMES = "names_v2"
    private const val KEY_AS_OF = "as_of_v2"
    private const val KEY_FROM = "from_code"
    private const val KEY_TO = "to_code"
    private const val KEY_PROVIDERS = "providers"
    private const val KEY_METAL_PROVIDERS_PREFIX = "metal_providers_"

    data class Snapshot(
        val rates: Map<String, Double>,
        val asOf: String
    )

    fun loadRates(context: Context): Snapshot? {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val ratesJson = prefs.getString(KEY_RATES, null) ?: return null
        val asOf = prefs.getString(KEY_AS_OF, null) ?: return null
        val json = JSONObject(ratesJson)
        val rates = mutableMapOf<String, Double>()
        for (code in json.keys()) {
            rates[code] = json.getDouble(code)
        }
        return Snapshot(rates, asOf)
    }

    fun saveRates(context: Context, rates: Map<String, Double>, asOf: String) {
        val json = JSONObject()
        for ((code, rate) in rates) {
            json.put(code, rate)
        }
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RATES, json.toString())
            .putString(KEY_AS_OF, asOf)
            .apply()
    }

    fun loadNames(context: Context): Map<String, String>? {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val namesJson = prefs.getString(KEY_NAMES, null) ?: return null
        val json = JSONObject(namesJson)
        val names = mutableMapOf<String, String>()
        for (code in json.keys()) {
            names[code] = json.getString(code)
        }
        return names
    }

    fun saveNames(context: Context, names: Map<String, String>) {
        val json = JSONObject()
        for ((code, name) in names) {
            json.put(code, name)
        }
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAMES, json.toString())
            .apply()
    }

    fun loadPair(context: Context): Pair<String, String> {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val from = prefs.getString(KEY_FROM, "USD") ?: "USD"
        val to = prefs.getString(KEY_TO, "EUR") ?: "EUR"
        return from to to
    }

    fun savePair(context: Context, fromCode: String, toCode: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FROM, fromCode)
            .putString(KEY_TO, toCode)
            .apply()
    }

    fun loadProviders(context: Context): Map<String, ProviderMeta>? {
        val json = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(KEY_PROVIDERS, null) ?: return null
        val obj = JSONObject(json)
        val result = mutableMapOf<String, ProviderMeta>()
        for (key in obj.keys()) {
            val entry = obj.getJSONObject(key)
            result[key] = ProviderMeta(
                key = key,
                name = entry.getString("name"),
                country = entry.optString("country").ifEmpty { null }
            )
        }
        return result
    }

    fun saveProviders(context: Context, providers: Map<String, ProviderMeta>) {
        val obj = JSONObject()
        for ((key, meta) in providers) {
            obj.put(
                key,
                JSONObject().put("name", meta.name).put("country", meta.country ?: "")
            )
        }
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PROVIDERS, obj.toString())
            .apply()
    }

    fun loadMetalProviders(context: Context, code: String): List<String>? {
        val json = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(KEY_METAL_PROVIDERS_PREFIX + code, null) ?: return null
        val array = JSONArray(json)
        return List(array.length()) { array.getString(it) }
    }

    fun saveMetalProviders(context: Context, code: String, keys: List<String>) {
        val array = JSONArray()
        for (key in keys) array.put(key)
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_METAL_PROVIDERS_PREFIX + code, array.toString())
            .apply()
    }
}
