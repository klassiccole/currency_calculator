package com.example.currency_counter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.currency_counter.ui.AppRoot
import com.example.currency_counter.ui.theme.CurrencyCalculatorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrencyCalculatorTheme {
                AppRoot()
            }
        }
    }
}
