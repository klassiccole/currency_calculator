package com.example.currency_counter.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

private data class Denomination(
    val label: String,
    val value: Double
)

private val Coins = listOf(
    Denomination("1¢", 0.01),
    Denomination("5¢", 0.05),
    Denomination("10¢", 0.10),
    Denomination("25¢", 0.25)
)

private val Bills = listOf(
    Denomination("$1", 1.0),
    Denomination("$5", 5.0),
    Denomination("$10", 10.0),
    Denomination("$20", 20.0),
    Denomination("$50", 50.0),
    Denomination("$100", 100.0)
)

private val AllDenominations = Coins + Bills

@Composable
fun CounterScreen(modifier: Modifier = Modifier) {
    var countTexts by rememberSaveable {
        mutableStateOf(AllDenominations.map { "" })
    }

    val total = countTexts.foldIndexed(0.0) { index, sum, text ->
        sum + (text.toLongOrNull() ?: 0L) * AllDenominations[index].value
    }
    val animatedTotal by animateFloatAsState(
        targetValue = total.toFloat(),
        label = "total"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TotalCard(
            total = animatedTotal,
            onClear = { countTexts = AllDenominations.map { "" } },
            hasCounts = countTexts.any { it.toLongOrNull() ?: 0L > 0L }
        )

        DenominationGroup(
            title = "Coins",
            denominations = Coins,
            offset = 0,
            countTexts = countTexts,
            onCountChange = { index, text ->
                countTexts = countTexts.mapIndexed { i, value ->
                    if (i == index) text else value
                }
            }
        )

        DenominationGroup(
            title = "Bills",
            denominations = Bills,
            offset = Coins.size,
            countTexts = countTexts,
            onCountChange = { index, text ->
                countTexts = countTexts.mapIndexed { i, value ->
                    if (i == index) text else value
                }
            }
        )
    }
}

@Composable
private fun TotalCard(
    total: Float,
    onClear: () -> Unit,
    hasCounts: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TOTAL USD",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = String.format(Locale.US, "$%,.2f", total),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (hasCounts) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Filled.RestartAlt,
                        contentDescription = "Clear all counts",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DenominationGroup(
    title: String,
    denominations: List<Denomination>,
    offset: Int,
    countTexts: List<String>,
    onCountChange: (index: Int, text: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            denominations.forEachIndexed { groupIndex, denomination ->
                if (groupIndex > 0) {
                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                val globalIndex = offset + groupIndex
                DenominationRow(
                    label = denomination.label,
                    countText = countTexts[globalIndex],
                    onCountChange = { onCountChange(globalIndex, it) }
                )
            }
        }
    }
}

@Composable
private fun DenominationRow(
    label: String,
    countText: String,
    onCountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = countText,
            onValueChange = { input ->
                if (input.all { it.isDigit() }) {
                    onCountChange(input)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
            modifier = Modifier.width(140.dp)
        )
    }
}
