package com.example.currency_counter.data

import android.content.Context

object ThemePreferences {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_DARK_MODE = "dark_mode_override"

    fun load(context: Context): Boolean? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return when (prefs.getString(KEY_DARK_MODE, null)) {
            "dark" -> true
            "light" -> false
            else -> null
        }
    }

    fun save(context: Context, darkMode: Boolean?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(
                KEY_DARK_MODE,
                when (darkMode) {
                    true -> "dark"
                    false -> "light"
                    null -> null
                }
            )
            .apply()
    }
}
