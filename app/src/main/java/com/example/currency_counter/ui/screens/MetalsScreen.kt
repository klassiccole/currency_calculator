package com.example.currency_counter.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.currency_counter.data.CurrencyRepository
import com.example.currency_counter.data.MetalPrice
import com.example.currency_counter.data.ProviderQuote
import com.example.currency_counter.ui.formatAmount
import kotlinx.coroutines.launch

private val METAL_COLORS = mapOf(
    "XAU" to Color(0xFFD9A94A),
    "XAG" to Color(0xFFC8CDD4),
    "XPT" to Color(0xFFE3E6E8),
    "XPD" to Color(0xFF9BA1A4)
)

@Composable
fun MetalsScreen(
    onOpenMetal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ratesState by CurrencyRepository.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        CurrencyRepository.refreshIfNeeded(context)
    }

    val metals = CurrencyRepository.metals().associateBy { it.code }
    val codes = listOf("XAU", "XAG", "XPT", "XPD")
    val names = mapOf("XAU" to "Gold", "XAG" to "Silver", "XPT" to "Platinum", "XPD" to "Palladium")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetalCard(
                code = codes[0],
                name = names.getValue(codes[0]),
                price = metals[codes[0]],
                onOpen = onOpenMetal,
                modifier = Modifier.weight(1f)
            )
            MetalCard(
                code = codes[1],
                name = names.getValue(codes[1]),
                price = metals[codes[1]],
                onOpen = onOpenMetal,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetalCard(
                code = codes[2],
                name = names.getValue(codes[2]),
                price = metals[codes[2]],
                onOpen = onOpenMetal,
                modifier = Modifier.weight(1f)
            )
            MetalCard(
                code = codes[3],
                name = names.getValue(codes[3]),
                price = metals[codes[3]],
                onOpen = onOpenMetal,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Divider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    ratesState.isLoading -> "Updating rates…"
                    ratesState.error != null && ratesState.asOf != null ->
                        "Couldn't update — rates from ${ratesState.asOf}"
                    ratesState.error != null -> "Couldn't update rates"
                    ratesState.asOf != null ->
                        "Rates as of ${ratesState.asOf} via Frankfurter."
                    else -> "Offline rates (approximate)."
                } + " Central-bank reference rates, not live market prices.",
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MetalCard(
    code: String,
    name: String,
    price: MetalPrice?,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onOpen(code) },
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
        ) {
            IngotIcon(code = code, ingotSize = 64.dp)
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = code,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (price != null) "$${formatAmount(price.pricePerOz)} / oz t" else "—",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (price != null) "$${formatAmount(price.pricePerGram)} / g" else "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MetalDetailScreen(
    code: String,
    modifier: Modifier = Modifier
) {
    val detail by CurrencyRepository.metalDetail.collectAsState()
    val ratesState by CurrencyRepository.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(code) {
        CurrencyRepository.loadMetalDetail(code, context)
    }

    val metal = CurrencyRepository.metal(code)
    val metalName = when (code) {
        "XAU" -> "Gold"
        "XAG" -> "Silver"
        "XPT" -> "Platinum"
        else -> "Palladium"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IngotIcon(code = code, ingotSize = 72.dp)
            Column {
                Text(
                    text = "$metalName ($code)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (ratesState.asOf != null)
                        "Blended rate as of ${ratesState.asOf}" else "Blended rate unavailable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (metal != null) {
                    Text(
                        text = "$${formatAmount(metal.pricePerOz)} per troy ounce",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$${formatAmount(metal.pricePerGram)} per gram",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Prices unavailable — check the rates screen",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "By reporting institute",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (detail.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.heightIn(min = 18.dp, max = 18.dp)
                )
            }
        }

        if (detail.error != null) {
            Text(
                text = detail.error ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (!detail.isLoading && detail.failedCount > 0) {
            Text(
                text = "${detail.failedCount} institutes failed to load",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        detail.quotes.forEach { quote ->
            ProviderRow(quote = quote)
        }
    }
}

@Composable
private fun ProviderRow(quote: ProviderQuote, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quote.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (quote.country != null) {
                    Text(
                        text = quote.country,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (quote.differsFromBlend) {
                    Text(
                        text = "differs from blended rate",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB98A2F)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${formatAmount(quote.pricePerOz)} / oz t",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$${formatAmount(quote.pricePerGram)} / g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "as of ${quote.asOf}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Divider()
    }
}

// Isometric trapezoid ingot: lit top face, mid front face, dark side face.
@Composable
private fun IngotIcon(code: String, ingotSize: Dp, modifier: Modifier = Modifier) {
    val base = METAL_COLORS.getValue(code)
    val topColor = lerp(base, Color.White, 0.35f)
    val frontColor = lerp(base, Color.Black, 0.08f)
    val sideColor = lerp(base, Color.Black, 0.32f)
    Canvas(modifier = modifier.size(ingotSize)) {
        val s = if (this.size.width < this.size.height) this.size.width else this.size.height
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val l2 = 0.22f * s
        val d2 = 0.16f * s
        val hgt = 0.30f * s
        val taper = 0.62f
        val kx = 0.866f
        val ky = 0.5f
        fun pt(x: Float, y: Float, z: Float) = Offset(
            cx + (x - y) * kx,
            cy + (x + y) * ky - z + 0.15f * s
        )
        fun topPt(x: Float, y: Float) = pt(x * taper, y * taper, hgt)
        val b1 = pt(-l2, d2, 0f)
        val b2 = pt(l2, d2, 0f)
        val b3 = pt(l2, -d2, 0f)
        val t1 = topPt(-l2, d2)
        val t2 = topPt(l2, d2)
        val t3 = topPt(l2, -d2)
        val t4 = topPt(-l2, -d2)
        drawPath(
            Path().apply {
                moveTo(t1.x, t1.y); lineTo(t2.x, t2.y)
                lineTo(b2.x, b2.y); lineTo(b1.x, b1.y); close()
            },
            frontColor
        )
        drawPath(
            Path().apply {
                moveTo(t2.x, t2.y); lineTo(t3.x, t3.y)
                lineTo(b3.x, b3.y); lineTo(b2.x, b2.y); close()
            },
            sideColor
        )
        drawPath(
            Path().apply {
                moveTo(t1.x, t1.y); lineTo(t2.x, t2.y)
                lineTo(t3.x, t3.y); lineTo(t4.x, t4.y); close()
            },
            topColor
        )
    }
}
