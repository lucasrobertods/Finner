package com.lrds.finner.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.lrds.finner.ui.navigation.FinnerRoute
import com.lrds.finner.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.lrds.finner.ui.navigation.TopLevelDestination
import com.lrds.finner.ui.theme.FinnerTheme
import java.util.Locale

// ---------------------------------------------------------------------------------------------
// Revolut-inspired circular badge palette for Budget Envelopes
// ---------------------------------------------------------------------------------------------
private val CustomBucketPalette = listOf(
    Color(0xFFF97316), // Revo Orange (Trip / custom)
    Color(0xFF06B6D4), // Cyan
    Color(0xFFEC4899), // Pink
    Color(0xFF6366F1), // Indigo
    Color(0xFF14B8A6), // Teal
    Color(0xFF84CC16)  // Lime
)

fun bucketColorFor(bucketName: String?): Color {
    if (bucketName.isNullOrBlank()) return Color(0xFF64748B)
    return when (bucketName.trim().lowercase()) {
        "bills" -> Color(0xFF3B82F6)    // Revo Blue
        "reserve" -> Color(0xFF00D09C)  // Stitch Mint
        "joint" -> Color(0xFFA855F7)    // Revo Purple
        "personal" -> Color(0xFFF59E0B) // Revo Amber
        "trip" -> Color(0xFFF97316)     // Revo Orange
        else -> {
            val hash = kotlin.math.abs(bucketName.trim().lowercase().hashCode())
            CustomBucketPalette[hash % CustomBucketPalette.size]
        }
    }
}

fun bucketIconFor(bucketName: String?): ImageVector {
    if (bucketName.isNullOrBlank()) return Icons.Default.AccountBalanceWallet
    return when (bucketName.trim().lowercase()) {
        "bills" -> Icons.Default.ReceiptLong
        "reserve" -> Icons.Default.Savings
        "joint" -> Icons.Default.Groups
        "personal" -> Icons.Default.Person
        "trip", "travel", "viagem" -> Icons.Default.Flight
        else -> Icons.Default.AccountBalanceWallet
    }
}

// ---------------------------------------------------------------------------------------------
// Reusable Stitch-style Dark/Light Card & Circular Quick Action Button
// ---------------------------------------------------------------------------------------------

@Composable
fun FinnerSectionCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val border = BorderStroke(1.dp, FinnerTheme.extendedColors.cardBorder)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = border
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = border
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

@Composable
fun CircularQuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (emphasized) {
                MaterialTheme.colorScheme.inverseSurface
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            border = if (emphasized) {
                null
            } else {
                BorderStroke(1.dp, FinnerTheme.extendedColors.cardBorder)
            },
            modifier = Modifier.size(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (emphasized) {
                        MaterialTheme.colorScheme.inverseOnSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------------------------
// 1. Top App Bar with Year Selector + Optional Trailing Slot (e.g. Exchange Rate Pill)
// ---------------------------------------------------------------------------------------------

@Composable
fun YearTitleSelector(
    selectedYear: Int,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit,
    onAddNextYear: () -> Unit,
    onDeleteYear: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val sortedYears = remember(availableYears, selectedYear) {
        (availableYears + selectedYear).distinct().sorted()
    }
    val nextYear = (sortedYears.maxOrNull() ?: selectedYear) + 1

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Finner $selectedYear",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select year",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sortedYears.forEach { year ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = year.toString(),
                                fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal,
                                color = if (year == selectedYear) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (sortedYears.size > 1) {
                                IconButton(
                                    onClick = {
                                        expanded = false
                                        onDeleteYear(year)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete year $year",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        onYearSelected(year)
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                text = {
                    Text(
                        text = "Add new ($nextYear)",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    expanded = false
                    onAddNextYear()
                }
            )
        }
    }
}

@Composable
fun FinnerTopBar(
    selectedYear: Int,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit,
    onAddNextYear: () -> Unit,
    onDeleteYear: (Int) -> Unit,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            YearTitleSelector(
                selectedYear = selectedYear,
                availableYears = availableYears,
                onYearSelected = onYearSelected,
                onAddNextYear = onAddNextYear,
                onDeleteYear = onDeleteYear
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.size(8.dp))
            trailingContent()
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 2. High-Contrast Revolut-Style Pill Month Tabs (JAN..DEC)
// ---------------------------------------------------------------------------------------------

@Composable
fun MonthPillTabs(
    months: List<String>,
    selectedIndex: Int,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem((selectedIndex - 1).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(months) { index, label ->
            MonthPill(
                label = label,
                selected = index == selectedIndex,
                onClick = { onMonthSelected(index) }
            )
        }
    }
}

@Composable
private fun MonthPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.inverseSurface
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = if (selected) {
            null
        } else {
            BorderStroke(1.dp, FinnerTheme.extendedColors.cardBorder)
        },
        modifier = Modifier
            .heightIn(min = 38.dp)
            .widthIn(min = 64.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.inverseOnSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 3. Compact Frosted Exchange-Rate Pill Chip ("1 € = R$ 5,50 ✎")
// ---------------------------------------------------------------------------------------------

@Composable
fun ExchangeRateChip(
    rate: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, FinnerTheme.extendedColors.cardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = String.format(Locale.forLanguageTag("pt-BR"), "1 € = R$ %.2f", rate),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit exchange rate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 4. Floating Auto-Hide Glass Bottom Navigation Dock (Navigation 3 backed)
// ---------------------------------------------------------------------------------------------

@Stable
class AutoHideBarState(private val thresholdPx: Float) {
    var isVisible by mutableStateOf(true)
        private set

    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            when {
                available.y < -thresholdPx -> isVisible = false
                available.y > thresholdPx -> isVisible = true
            }
            return Offset.Zero
        }
    }

    fun show() {
        isVisible = true
    }
}

@Composable
fun rememberAutoHideBarState(thresholdPx: Float = 6f): AutoHideBarState =
    remember(thresholdPx) { AutoHideBarState(thresholdPx) }

@Composable
fun FloatingBottomNav(
    selectedRoute: NavKey,
    onSelected: (FinnerRoute) -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut()
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = FinnerTheme.extendedColors.navDockBackground,
            border = BorderStroke(1.dp, FinnerTheme.extendedColors.cardBorder),
            shadowElevation = 16.dp,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TOP_LEVEL_DESTINATIONS.forEach { destination ->
                    FloatingNavItem(
                        destination = destination,
                        selected = destination.route == selectedRoute,
                        onClick = { onSelected(destination.route) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_tab_${destination.label.lowercase()}")
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        color = if (selected) activeBg else Color.Transparent,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                modifier = Modifier.size(22.dp),
                tint = contentColor
            )
            Text(
                text = destination.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 5. Ambient Revolut Top Aura Modifier & Edge-to-Edge Floating Dock Scaffold
// ---------------------------------------------------------------------------------------------

@Composable
fun Modifier.finnerTopAura(enabled: Boolean = true): Modifier {
    val background = MaterialTheme.colorScheme.background
    val auraStart = FinnerTheme.extendedColors.heroGradientStart
    return this
        .background(background)
        .then(
            if (enabled) {
                Modifier.background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to auraStart.copy(alpha = 0.45f),
                            0.28f to auraStart.copy(alpha = 0.12f),
                            0.52f to Color.Transparent
                        )
                    )
                )
            } else {
                Modifier
            }
        )
}

private val NavBarSlotHeight = 84.dp
private val FabGap = 12.dp
private val EdgeGap = 16.dp

@Composable
fun FinnerFloatingScaffold(
    selectedTopLevelRoute: NavKey,
    onDestinationSelected: (FinnerRoute) -> Unit,
    showBottomBar: Boolean,
    fabLabel: String?,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    barState: AutoHideBarState = rememberAutoHideBarState(),
    content: @Composable () -> Unit
) {
    LaunchedEffect(selectedTopLevelRoute, showBottomBar) {
        if (showBottomBar) {
            barState.show()
        }
    }

    val effectiveBottomBarVisible = showBottomBar && barState.isVisible
    val fabBottomPadding by animateDpAsState(
        targetValue = if (effectiveBottomBarVisible) NavBarSlotHeight + FabGap else EdgeGap,
        label = "fabBottomPadding"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(barState.connection)
    ) {
        content()

        AnimatedVisibility(
            visible = fabLabel != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = EdgeGap, bottom = fabBottomPadding)
        ) {
            ExtendedFloatingActionButton(
                onClick = onFabClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(fabLabel.orEmpty(), fontWeight = FontWeight.Bold) },
                shape = CircleShape,
                modifier = Modifier.testTag("finner_fab")
            )
        }

        FloatingBottomNav(
            selectedRoute = selectedTopLevelRoute,
            onSelected = onDestinationSelected,
            visible = effectiveBottomBarVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}
