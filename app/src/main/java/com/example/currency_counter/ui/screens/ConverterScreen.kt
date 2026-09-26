package com.example.currency_counter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.currency_counter.data.CurrencyRepository

@Composable
fun ConverterScreen(modifier: Modifier = Modifier) {
    var amountText by rememberSaveable { mutableStateOf("") }
    var fromCode by rememberSaveable { mutableStateOf("USD") }
    var toCode by rememberSaveable { mutableStateOf("EUR") }

    val amount = amountText.toDoubleOrNull()
    val result = if (amount != null) {
        CurrencyRepository.convert(
            amount = amount,
            from = CurrencyRepository.byCode(fromCode),
            to = CurrencyRepository.byCode(toCode)
        )
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurrencyPicker(
                label = "From",
                selectedCode = fromCode,
                onSelect = { fromCode = it },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    val oldFrom = fromCode
                    fromCode = toCode
                    toCode = oldFrom
                }
            ) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = "Swap currencies")
            }
            CurrencyPicker(
                label = "To",
                selectedCode = toCode,
                onSelect = { toCode = it },
                modifier = Modifier.weight(1f)
            )
        }

        if (result != null) {
            Text(
                text = "%.2f %s = %.2f %s".format(
                    amount, fromCode, result, toCode
                ),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = "Offline rates (approximate)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPicker(
    label: String,
    selectedCode: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCode,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CurrencyRepository.all.forEach { currency ->
                DropdownMenuItem(
                    text = { Text("${currency.code} - ${currency.name}") },
                    onClick = {
                        onSelect(currency.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
