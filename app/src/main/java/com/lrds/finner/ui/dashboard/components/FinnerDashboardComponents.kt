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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
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
import androidx.compose.ui.text.style.TextOverflow
import com.lrds.finner.ui.theme.FinnerAlpha
import com.lrds.finner.ui.theme.FinnerTheme
import java.util.Locale

// ---------------------------------------------------------------------------------------------
// Theme-aware semantic helpers for Budget Envelopes
// ---------------------------------------------------------------------------------------------

@Composable
@ReadOnlyComposable
fun bucketColorFor(bucketName: String?): Color =
    FinnerTheme.colors.buckets.forName(bucketName)

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
    contentPadding: PaddingValues = PaddingValues(FinnerTheme.spacing.cardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = FinnerTheme.shapes.sectionCard
    val border = BorderStroke(FinnerTheme.sizes.borderThin, FinnerTheme.colors.cardBorder)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = FinnerTheme.colors.surfaceContainer,
            border = border,
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content,
            )
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = FinnerTheme.colors.surfaceContainer,
            border = border,
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content,
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
    emphasized: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FinnerTheme.spacing.sm),
    ) {
        Surface(
            onClick = onClick,
            shape = FinnerTheme.shapes.pill,
            color = if (emphasized) {
                FinnerTheme.colors.inverseSurface
            } else {
                FinnerTheme.colors.surfaceContainerHigh
            },
            border = if (emphasized) {
                null
            } else {
                BorderStroke(FinnerTheme.sizes.borderThin, FinnerTheme.colors.cardBorder)
            },
            modifier = Modifier.size(FinnerTheme.sizes.quickActionButton),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (emphasized) {
                        FinnerTheme.colors.inverseOnSurface
                    } else {
                        FinnerTheme.colors.onSurface
                    },
                    modifier = Modifier.size(FinnerTheme.sizes.iconXl),
                )
            }
        }
        Text(
            text = label,
            style = FinnerTheme.typography.labelMedium,
            color = FinnerTheme.colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
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
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val sortedYears = remember(availableYears, selectedYear) {
        (availableYears + selectedYear).distinct().sorted()
    }
    val nextYear = (sortedYears.maxOrNull() ?: selectedYear) + 1

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(FinnerTheme.shapes.rowItem)
                .clickable { expanded = true }
                .padding(vertical = FinnerTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FinnerTheme.spacing.xxs),
        ) {
            Text(
                text = "Finner $selectedYear",
                style = FinnerTheme.typography.headlineMedium,
                color = FinnerTheme.colors.onSurface,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select year",
                tint = FinnerTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(FinnerTheme.sizes.iconXxl),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            sortedYears.forEach { year ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = year.toString(),
                                style = if (year == selectedYear) {
                                    FinnerTheme.typography.bodyMediumBold
                                } else {
                                    FinnerTheme.typography.bodyMedium
                                },
                                color = if (year == selectedYear) {
                                    FinnerTheme.colors.primary
                                } else {
                                    FinnerTheme.colors.onSurface
                                },
                            )
                            if (sortedYears.size > 1) {
                                IconButton(
                                    onClick = {
                                        expanded = false
                                        onDeleteYear(year)
                                    },
                                    modifier = Modifier.size(FinnerTheme.sizes.iconButtonMd),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete year $year",
                                        tint = FinnerTheme.colors.error,
                                        modifier = Modifier.size(FinnerTheme.sizes.iconMd),
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        expanded = false
                        onYearSelected(year)
                    },
                )
            }
            HorizontalDivider(color = FinnerTheme.colors.cardBorder)
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = FinnerTheme.colors.primary,
                    )
                },
                text = {
                    Text(
                        text = "Add new ($nextYear)",
                        style = FinnerTheme.typography.bodyMediumSemiBold,
                        color = FinnerTheme.colors.primary,
                    )
                },
                onClick = {
                    expanded = false
                    onAddNextYear()
                },
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
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                horizontal = FinnerTheme.spacing.topBarHorizontal,
                vertical = FinnerTheme.spacing.topBarVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            YearTitleSelector(
                selectedYear = selectedYear,
                availableYears = availableYears,
                onYearSelected = onYearSelected,
                onAddNextYear = onAddNextYear,
                onDeleteYear = onDeleteYear,
            )
            Text(
                text = subtitle,
                style = FinnerTheme.typography.bodySmall,
                color = FinnerTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.size(FinnerTheme.spacing.sm))
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
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem((selectedIndex - 1).coerceAtLeast(0))
    }

    LazyRow(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = FinnerTheme.spacing.topBarHorizontal,
            vertical = FinnerTheme.spacing.compactPillVertical,
        ),
        horizontalArrangement = Arrangement.spacedBy(FinnerTheme.spacing.sm),
    ) {
        itemsIndexed(months) { index, label ->
            MonthPill(
                label = label,
                selected = index == selectedIndex,
                onClick = { onMonthSelected(index) },
            )
        }
    }
}

@Composable
private fun MonthPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = FinnerTheme.shapes.pill,
        color = if (selected) {
            FinnerTheme.colors.inverseSurface
        } else {
            FinnerTheme.colors.surfaceContainerHigh
        },
        border = if (selected) {
            null
        } else {
            BorderStroke(FinnerTheme.sizes.borderThin, FinnerTheme.colors.cardBorder)
        },
        modifier = Modifier
            .heightIn(min = FinnerTheme.sizes.pillMinHeight)
            .widthIn(min = FinnerTheme.sizes.monthPillMinWidth),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(
                horizontal = FinnerTheme.spacing.pillHorizontal,
                vertical = FinnerTheme.spacing.pillVertical,
            ),
        ) {
            Text(
                text = label,
                style = if (selected) {
                    FinnerTheme.typography.labelLargeBold
                } else {
                    FinnerTheme.typography.labelLargeMedium
                },
                color = if (selected) {
                    FinnerTheme.colors.inverseOnSurface
                } else {
                    FinnerTheme.colors.onSurfaceVariant
                },
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
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = FinnerTheme.shapes.pill,
        color = FinnerTheme.colors.surfaceContainerHigh,
        border = BorderStroke(FinnerTheme.sizes.borderThin, FinnerTheme.colors.cardBorder),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = FinnerTheme.spacing.compactPillHorizontal,
                vertical = FinnerTheme.spacing.compactPillVertical,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FinnerTheme.spacing.compactPillVertical),
        ) {
            Text(
                text = String.format(Locale.forLanguageTag("pt-BR"), "1 € = R$ %.2f", rate),
                style = FinnerTheme.typography.labelMedium,
                color = FinnerTheme.colors.onSurface,
                maxLines = 1,
                softWrap = false,
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit exchange rate",
                tint = FinnerTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(FinnerTheme.sizes.iconXs),
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 4. Floating Auto-Hide Glass Bottom Navigation Dock
// ---------------------------------------------------------------------------------------------

enum class FinnerDestination(val label: String, val icon: ImageVector) {
    MESES("Months", Icons.Default.CalendarMonth),
    OBJETIVOS("Goals", Icons.Default.TrackChanges),
    HISTORICO("History", Icons.Default.History),
    PERFIL("Profile", Icons.Default.Person),
}

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
    selected: FinnerDestination,
    onSelected: (FinnerDestination) -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it * 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it * 2 }) + fadeOut(),
    ) {
        Surface(
            shape = FinnerTheme.shapes.navDock,
            color = FinnerTheme.colors.navDockBackground,
            border = BorderStroke(FinnerTheme.sizes.borderThin, FinnerTheme.colors.cardBorder),
            shadowElevation = FinnerTheme.elevation.floatingDock,
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = FinnerTheme.spacing.screenHorizontal)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = FinnerTheme.spacing.compactPillVertical,
                    vertical = FinnerTheme.spacing.compactPillVertical,
                ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FinnerDestination.entries.forEach { destination ->
                    FloatingNavItem(
                        destination = destination,
                        selected = destination == selected,
                        onClick = { onSelected(destination) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    destination: FinnerDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeBg = FinnerTheme.colors.primary.copy(alpha = FinnerAlpha.tintedContainer)
    val contentColor = if (selected) {
        FinnerTheme.colors.primary
    } else {
        FinnerTheme.colors.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = FinnerTheme.shapes.navDockItem,
        color = if (selected) activeBg else Color.Transparent,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = FinnerTheme.spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FinnerTheme.spacing.xxs),
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.label,
                modifier = Modifier.size(FinnerTheme.sizes.iconXl),
                tint = contentColor,
            )
            Text(
                text = destination.label,
                style = if (selected) {
                    FinnerTheme.typography.labelSmallBold
                } else {
                    FinnerTheme.typography.labelSmallMedium
                },
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 5. Scaffold with Ambient Revolut Top Aura + Floating Dock
// ---------------------------------------------------------------------------------------------

@Composable
fun FinnerFloatingScaffold(
    topBar: @Composable () -> Unit,
    selectedDestination: FinnerDestination,
    onDestinationSelected: (FinnerDestination) -> Unit,
    fabLabel: String?,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBottomBar: Boolean = true,
    showTopAura: Boolean = true,
    content: @Composable () -> Unit,
) {
    val barState = rememberAutoHideBarState()
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val colors = FinnerTheme.colors

    LaunchedEffect(selectedDestination, showBottomBar) { barState.show() }

    val fabBottomPadding by animateDpAsState(
        targetValue = if (showBottomBar && barState.isVisible) {
            sizes.floatingNavSlotHeight + spacing.fabGap
        } else {
            spacing.screenHorizontal
        },
        label = "fabBottomPadding",
    )

    val auraStart = colors.heroGradientStart

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .then(
                if (showTopAura) {
                    Modifier.background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to auraStart.copy(alpha = FinnerAlpha.auraTop),
                                0.28f to auraStart.copy(alpha = FinnerAlpha.auraMid),
                                0.52f to Color.Transparent,
                            ),
                        ),
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(barState.connection),
            containerColor = Color.Transparent,
            topBar = topBar,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                content()

                if (fabLabel != null) {
                    ExtendedFloatingActionButton(
                        onClick = onFabClick,
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text(fabLabel, style = FinnerTheme.typography.labelLargeBold) },
                        shape = FinnerTheme.shapes.pill,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .navigationBarsPadding()
                            .padding(end = spacing.screenHorizontal, bottom = fabBottomPadding),
                    )
                }

                if (showBottomBar) {
                    FloatingBottomNav(
                        selected = selectedDestination,
                        onSelected = onDestinationSelected,
                        visible = barState.isVisible,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = spacing.floatingNavBottomMargin),
                    )
                }
            }
        }
    }
}
