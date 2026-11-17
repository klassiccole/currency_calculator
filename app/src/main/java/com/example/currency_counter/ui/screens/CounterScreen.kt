@file:OptIn(ExperimentalFoundationApi::class)

package com.example.currency_counter.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.currency_counter.ui.QuickEntryKeypad
import com.example.currency_counter.ui.theme.DarkGreenInk
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

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

private const val WheelCycle = 10
private const val WheelRepeats = 7
private val WheelMiddleStart = (WheelRepeats / 2) * WheelCycle
private val WheelItemHeight = 40.dp
private val WheelHeight = WheelItemHeight * 3
private val WheelWidth = 44.dp
private val WheelGap = 6.dp
private val WheelShape = RoundedCornerShape(10.dp)
private val BadgeShape = RoundedCornerShape(12.dp)
private val BadgeWidth = 80.dp
private val CoinBadgeSize = 66.dp

private val CoinFaceLight = Color(0xFFD7E7CF)
private val CoinFaceDark = Color(0xFFAECBA9)
private val BillFaceLight = Color(0xFFA9D3B2)
private val BillFaceDark = Color(0xFF8CBE97)

@Composable
fun CounterScreen(modifier: Modifier = Modifier) {
    var counts by rememberSaveable {
        mutableStateOf(AllDenominations.map { 0 })
    }
    var keypadOpen by rememberSaveable { mutableStateOf(false) }
    var activeIndex by rememberSaveable { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val scrollContainerY = remember { mutableStateOf(0f) }
    val rowYs = remember { mutableStateMapOf<Int, Float>() }

    fun setCount(index: Int, value: Int) {
        counts = counts.mapIndexed { i, count ->
            if (i == index) max(0, value) else count
        }
    }

    val total = counts.foldIndexed(0.0) { index, sum, count ->
        sum + count * AllDenominations[index].value
    }
    val animatedTotal by animateFloatAsState(
        targetValue = total.toFloat(),
        label = "total"
    )

    LaunchedEffect(activeIndex, keypadOpen) {
        if (keypadOpen) {
            delay(120)
            rowYs[activeIndex]?.let { y ->
                scrollState.animateScrollTo((y - scrollContainerY.value).roundToInt())
            }
        }
    }

    BackHandler(enabled = keypadOpen) { keypadOpen = false }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TotalCard(
                total = animatedTotal,
                onClear = { counts = AllDenominations.map { 0 } },
                hasCounts = counts.any { it > 0 }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { layoutCoordinates ->
                        scrollContainerY.value = layoutCoordinates.positionInRoot().y
                    },
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DenominationGroup(
                    title = "Coins",
                    denominations = Coins,
                    offset = 0,
                    isCoin = true,
                    counts = counts,
                    activeIndex = activeIndex,
                    selectionEnabled = keypadOpen,
                    onSelectRow = { activeIndex = it },
                    onRowPosition = { index, y -> rowYs[index] = y },
                    onCountChange = ::setCount
                )

                DenominationGroup(
                    title = "Bills",
                    denominations = Bills,
                    offset = Coins.size,
                    isCoin = false,
                    counts = counts,
                    activeIndex = activeIndex,
                    selectionEnabled = keypadOpen,
                    onSelectRow = { activeIndex = it },
                    onRowPosition = { index, y -> rowYs[index] = y },
                    onCountChange = ::setCount
                )
            }
        }

        if (!keypadOpen) {
            FloatingActionButton(
                onClick = {
                    val firstEmpty = counts.indexOfFirst { it == 0 }
                    activeIndex = if (firstEmpty >= 0) firstEmpty else 0
                    keypadOpen = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Dialpad,
                    contentDescription = "Open quick entry keypad"
                )
            }
        }

        AnimatedVisibility(
            visible = keypadOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            QuickEntryKeypad(
                activeLabel = AllDenominations[activeIndex].label,
                onDigit = { digit ->
                    setCount(activeIndex, min(999, counts[activeIndex] * 10 + digit))
                },
                onBackspace = { setCount(activeIndex, counts[activeIndex] / 10) },
                onClear = { setCount(activeIndex, 0) },
                onNext = { activeIndex = (activeIndex + 1) % AllDenominations.size },
                onClose = { keypadOpen = false }
            )
        }
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
    isCoin: Boolean,
    counts: List<Int>,
    activeIndex: Int,
    selectionEnabled: Boolean,
    onSelectRow: (Int) -> Unit,
    onRowPosition: (index: Int, y: Float) -> Unit,
    onCountChange: (index: Int, value: Int) -> Unit,
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
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
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
                    count = counts[globalIndex],
                    isCoin = isCoin,
                    isActive = selectionEnabled && globalIndex == activeIndex,
                    selectionEnabled = selectionEnabled,
                    onSelect = { onSelectRow(globalIndex) },
                    onCountChange = { onCountChange(globalIndex, it) },
                    onPosition = { y -> onRowPosition(globalIndex, y) }
                )
            }
        }
    }
}

@Composable
private fun DenominationRow(
    label: String,
    count: Int,
    isCoin: Boolean,
    isActive: Boolean,
    selectionEnabled: Boolean,
    onSelect: () -> Unit,
    onCountChange: (Int) -> Unit,
    onPosition: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .onGloballyPositioned { layoutCoordinates ->
                onPosition(layoutCoordinates.positionInRoot().y)
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DenominationBadge(
            label = label,
            isCoin = isCoin,
            selected = isActive,
            onClick = if (selectionEnabled) onSelect else null
        )
        Text(
            text = "\u00D7",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 36.dp)
        )
        WheelCluster(
            count = count,
            label = label,
            highlighted = isActive,
            onClick = if (selectionEnabled) onSelect else null,
            onCountChange = onCountChange
        )
    }
}

@Composable
private fun DenominationBadge(
    label: String,
    isCoin: Boolean,
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val shape = if (isCoin) CircleShape else BadgeShape
    val faceLight = if (isCoin) CoinFaceLight else BillFaceLight
    val faceDark = if (isCoin) CoinFaceDark else BillFaceDark
    Box(
        modifier = modifier
            .then(
                if (isCoin) Modifier.size(CoinBadgeSize) else Modifier.width(BadgeWidth)
            )
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(faceLight, faceDark)))
            .padding(3.dp)
            .then(
                if (selected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                } else {
                    Modifier
                }
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = 0.30f),
                        0.35f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.10f)
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(1.dp, DarkGreenInk.copy(alpha = 0.45f), shape)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(2.dp)
                .border(1.dp, DarkGreenInk.copy(alpha = 0.25f), shape)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkGreenInk
        )
    }
}

@Composable
private fun WheelCluster(
    count: Int,
    label: String,
    highlighted: Boolean,
    onClick: (() -> Unit)?,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val digits = count.toString().padStart(3, '0')
    val clusterShape = RoundedCornerShape(14.dp)

    Row(
        modifier = modifier
            .clip(clusterShape)
            .then(
                if (highlighted) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, clusterShape)
                } else {
                    Modifier
                }
            )
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(WheelGap)
    ) {
        digits.forEachIndexed { displayIndex, digitChar ->
            val placeIndex = digits.length - 1 - displayIndex
            val oldDigit = digitChar - '0'
            val placeValue = pow10(placeIndex)
            DigitWheel(
                digit = oldDigit,
                placeName = placeName(placeIndex),
                label = label,
                onDigitChange = { newDigit ->
                    onCountChange(
                        count - oldDigit * placeValue + newDigit * placeValue
                    )
                }
            )
        }
    }
}

@Composable
private fun DigitWheel(
    digit: Int,
    placeName: String,
    label: String,
    onDigitChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = WheelMiddleStart + digit - 1
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val currentDigit by rememberUpdatedState(digit)
    val currentOnDigitChange by rememberUpdatedState(onDigitChange)
    val maxDistancePx = with(LocalDensity.current) {
        WheelItemHeight.toPx() * 1.5f
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            Triple(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
                listState.isScrollInProgress
            )
        }.collect { (index, offset, scrolling) ->
            if (scrolling || offset != 0) return@collect
            val selected = index + 1
            val d = ((selected % WheelCycle) + WheelCycle) % WheelCycle
            if (d != currentDigit) {
                currentOnDigitChange(d)
            }
            if (selected < WheelMiddleStart || selected >= WheelMiddleStart + WheelCycle) {
                listState.scrollToItem(WheelMiddleStart + d - 1)
            }
        }
    }

    LaunchedEffect(digit) {
        val selected = listState.firstVisibleItemIndex + 1
        if (!listState.isScrollInProgress && selected % WheelCycle != digit) {
            listState.scrollToItem(WheelMiddleStart + digit - 1)
        }
    }

    Box(
        modifier = modifier
            .width(WheelWidth)
            .height(WheelHeight)
            .clip(WheelShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = WheelShape
            )
            .semantics {
                contentDescription = "$label $placeName digit wheel"
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(WheelItemHeight)
                .align(Alignment.Center)
                .background(
                    Brush.horizontalGradient(
                        0f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0f),
                        0.2f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        0.8f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        1f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0f)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.Center)
                .offset(y = -WheelItemHeight / 2)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.Center)
                .offset(y = WheelItemHeight / 2)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        )
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                count = WheelRepeats * WheelCycle,
                key = { it }
            ) { index ->
                val value = index % WheelCycle
                val alpha by remember(index) {
                    derivedStateOf {
                        val info = listState.layoutInfo.visibleItemsInfo
                            .firstOrNull { it.index == index }
                            ?: return@derivedStateOf 0f
                        val viewportCenter = listState.layoutInfo.viewportSize.height / 2f
                        val itemCenter = info.offset + info.size / 2f
                        val distance = abs(itemCenter - viewportCenter)
                        (1f - distance / maxDistancePx).coerceIn(0f, 1f)
                    }
                }
                Box(
                    modifier = Modifier
                        .height(WheelItemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                        modifier = Modifier.graphicsLayer {
                            val scale = 0.82f + 0.18f * alpha
                            scaleX = scale
                            scaleY = scale
                        }
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.18f),
                        0.3f to Color.Transparent,
                        0.7f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.18f)
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.10f),
                        0.3f to Color.Transparent,
                        0.7f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.10f)
                    )
                )
        )
    }
}

private fun placeName(placeIndex: Int): String = when (placeIndex) {
    0 -> "ones"
    1 -> "tens"
    else -> "hundreds"
}

private fun pow10(power: Int): Int = when (power) {
    0 -> 1
    1 -> 10
    2 -> 100
    else -> 1000
}
