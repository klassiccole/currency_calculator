package com.example.currency_counter.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Settings coming soon.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}
