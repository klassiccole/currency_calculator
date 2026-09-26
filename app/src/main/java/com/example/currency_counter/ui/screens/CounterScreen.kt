package com.example.currency_counter.ui.screens

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.currency_counter.R

@Composable
fun CounterScreen(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.activity_main, null)
            bindCounterViews(view)
            view
        }
    )
}

private fun bindCounterViews(root: View) {
    val inputs = mapOf(
        root.findViewById<EditText>(R.id.input_one_cent) to 0.01,
        root.findViewById<EditText>(R.id.input_five_cents) to 0.05,
        root.findViewById<EditText>(R.id.input_ten_cents) to 0.10,
        root.findViewById<EditText>(R.id.input_twentyfive_cents) to 0.25,
        root.findViewById<EditText>(R.id.input_one_dollar) to 1.0,
        root.findViewById<EditText>(R.id.input_five_dollar) to 5.0,
        root.findViewById<EditText>(R.id.input_ten_dollar) to 10.0,
        root.findViewById<EditText>(R.id.input_twenty_dollar) to 20.0,
        root.findViewById<EditText>(R.id.input_fifty_dollar) to 50.0,
        root.findViewById<EditText>(R.id.input_hundred_dollar) to 100.0
    )
    val totalTextView = root.findViewById<TextView>(R.id.total_text)

    fun calculateTotal() {
        val total = inputs.entries.sumOf { (field, value) ->
            parseCount(field) * value
        }
        totalTextView.text = String.format("Total: $%.2f", total)
    }

    inputs.keys.forEach { field ->
        field.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateTotal()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}

private fun parseCount(field: EditText): Double {
    return field.text.toString().toDoubleOrNull() ?: 0.0
}
