package com.example.currency_counter.data

import android.content.Context
import org.json.JSONObject

// Small SharedPreferences-backed cache: last successful rates + currency names,
// so the app shows real last-known rates on offline cold starts instead of the
// hardcoded fallback list.
object CurrencyPreferences {
    private const val FILE = "currency_prefs"
    private const val KEY_RATES = "rates"
    private const val KEY_NAMES = "names"
    private const val KEY_AS_OF = "as_of"
    private const val KEY_FROM = "from_code"
    private const val KEY_TO = "to_code"

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
}
