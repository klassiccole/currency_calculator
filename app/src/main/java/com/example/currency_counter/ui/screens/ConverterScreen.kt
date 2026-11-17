@file:OptIn(ExperimentalComposeUiApi::class)

package com.example.currency_counter.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.currency_counter.data.Currency
import com.example.currency_counter.data.CurrencyPreferences
import com.example.currency_counter.data.CurrencyRepository
import com.example.currency_counter.ui.QuickEntryKeypad
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ConverterScreen(modifier: Modifier = Modifier) {
    val ratesState by CurrencyRepository.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        CurrencyRepository.refreshIfNeeded(context)
    }

    var fromText by rememberSaveable { mutableStateOf("") }
    var toText by rememberSaveable { mutableStateOf("") }
    var lastEditedFrom by rememberSaveable { mutableStateOf(true) }
    val (savedFrom, savedTo) = remember { CurrencyPreferences.loadPair(context) }
    var fromCode by rememberSaveable { mutableStateOf(savedFrom) }
    var toCode by rememberSaveable { mutableStateOf(savedTo) }
    var keypadOpen by rememberSaveable { mutableStateOf(false) }
    var activeField by rememberSaveable { mutableStateOf("from") }
    val keyboard = LocalSoftwareKeyboardController.current

    BackHandler(enabled = keypadOpen) { keypadOpen = false }

    val from = ratesState.currencies.firstOrNull { it.code == fromCode }
    val to = ratesState.currencies.firstOrNull { it.code == toCode }

    fun convertedText(text: String, source: Currency?, target: Currency?): String {
        val value = parseAmount(text) ?: return ""
        if (source == null || target == null) return ""
        return formatConverted(CurrencyRepository.convert(value, source, target))
    }

    fun copyValue(text: String) {
        val value = text.replace(",", "")
        if (value.isEmpty()) return
        clipboard.setText(AnnotatedString(value))
        Toast.makeText(context, "Copied $value", Toast.LENGTH_SHORT).show()
    }

    fun setAmountText(isFrom: Boolean, newText: String) {
        if (isFrom) {
            fromText = newText
            lastEditedFrom = true
            toText = convertedText(newText, from, to)
        } else {
            toText = newText
            lastEditedFrom = false
            fromText = convertedText(newText, to, from)
        }
    }

    fun openKeypad(field: String) {
        keyboard?.hide()
        activeField = field
        keypadOpen = true
    }

    val fromFieldInteraction = remember { MutableInteractionSource() }
    LaunchedEffect(fromFieldInteraction) {
        fromFieldInteraction.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) openKeypad("from")
        }
    }
    val toFieldInteraction = remember { MutableInteractionSource() }
    LaunchedEffect(toFieldInteraction) {
        toFieldInteraction.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) openKeypad("to")
        }
    }

    LaunchedEffect(ratesState.currencies, fromCode, toCode) {
        if (lastEditedFrom) {
            toText = convertedText(fromText, from, to)
        } else {
            fromText = convertedText(toText, to, from)
        }
    }
    LaunchedEffect(fromCode, toCode) {
        CurrencyPreferences.savePair(context, fromCode, toCode)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fromText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Amount") },
                    interactionSource = fromFieldInteraction,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = toText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Converted") },
                    interactionSource = toFieldInteraction,
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { copyValue(toText) }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy converted value")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencyPicker(
                    label = "From",
                    selectedCode = fromCode,
                    currencies = ratesState.currencies,
                    onSelect = { fromCode = it },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        val oldFrom = fromCode
                        fromCode = toCode
                        toCode = oldFrom
                        val oldText = fromText
                        fromText = toText
                        toText = oldText
                        lastEditedFrom = !lastEditedFrom
                    }
                ) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = "Swap conversion direction")
                }
                CurrencyPicker(
                    label = "To",
                    selectedCode = toCode,
                    currencies = ratesState.currencies,
                    onSelect = { toCode = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        ratesState.isLoading -> "Updating rates…"
                        ratesState.error != null && ratesState.asOf != null ->
                            "Couldn't update — rates from ${ratesState.asOf}"
                        ratesState.error != null -> "Couldn't update rates — using offline rates"
                        ratesState.asOf != null -> "Rates as of ${ratesState.asOf} (Frankfurter)"
                        else -> "Offline rates (approximate)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (ratesState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(start = 8.dp).heightIn(min = 18.dp, max = 18.dp)
                    )
                } else {
                    IconButton(onClick = { scope.launch { CurrencyRepository.refresh(context) } }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh rates")
                    }
                }
        }
        }

        AnimatedVisibility(
            visible = keypadOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            QuickEntryKeypad(
                title = "Enter amount",
                activeLabel = if (activeField == "from") "Amount" else "Converted",
                onDigit = { digit ->
                    val isFrom = activeField == "from"
                    val current = if (isFrom) fromText else toText
                    setAmountText(isFrom, if (current == "0") digit.toString() else current + digit)
                },
                onDecimal = {
                    val isFrom = activeField == "from"
                    val current = if (isFrom) fromText else toText
                    when {
                        current.isEmpty() -> setAmountText(isFrom, "0.")
                        !current.contains('.') -> setAmountText(isFrom, "$current.")
                    }
                },
                onBackspace = {
                    val isFrom = activeField == "from"
                    val current = if (isFrom) fromText else toText
                    if (current.isNotEmpty()) setAmountText(isFrom, current.dropLast(1))
                },
                onClear = { setAmountText(activeField == "from", "") },
                onNext = { activeField = if (activeField == "from") "to" else "from" },
                onClose = { keypadOpen = false }
            )
        }
    }
}

// Two decimals for normal magnitudes (with thousands separators), four for
// results too small for 2dp to mean anything.
private fun formatConverted(value: Double): String {
    val pattern = if (value >= 1.0 || value <= -1.0) "%,.2f" else "%.4f"
    return String.format(Locale.US, pattern, value)
}

// Typed text may contain the thousands separators we render into derived values;
// grouping is display-only, so strip it before parsing.
private fun parseAmount(text: String): Double? =
    text.replace(",", "").toDoubleOrNull()

@Composable
private fun CurrencyPicker(
    label: String,
    selectedCode: String,
    currencies: List<Currency>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) showDialog = true
        }
    }

    OutlinedTextField(
        value = selectedCode,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth()
    )

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .imePadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select $label currency",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Search") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            currencies.filter {
                                it.code.contains(query, ignoreCase = true) ||
                                    it.name.contains(query, ignoreCase = true)
                            },
                            key = { it.code }
                        ) { currency ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(currency.code)
                                        showDialog = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currency.code,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(
                                    text = currency.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (currency.code == selectedCode) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}
