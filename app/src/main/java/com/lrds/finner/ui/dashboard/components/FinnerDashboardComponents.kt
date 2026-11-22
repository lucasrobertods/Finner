package com.lrds.finner.ui.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

// ---------------------------------------------------------------------------------------------
// Palette used by the pill tabs / chips (light blue fill + dark blue outline, per the mockup)
// ---------------------------------------------------------------------------------------------
private val PillActiveContainer = Color(0xFFD3E8F8)
private val PillActiveBorder = Color(0xFF1E5E8C)
private val PillActiveContent = Color(0xFF0B2F4A)
private val PillInactiveBorder = Color(0xFF9AA0A6)

private val CustomBucketPalette = listOf(
    Color(0xFF26A69A), // Teal
    Color(0xFFEC407A), // Pink
    Color(0xFF5C6BC0), // Indigo
    Color(0xFF8D6E63), // Warm Brown
    Color(0xFF29B6F6), // Light Blue
    Color(0xFFD4E157)  // Lime
)

/**
 * Returns a distinct, high-contrast color for each bucket (Bills, Reserve, Joint, Personal,
 * or any user-created bucket) so buckets are easy to identify across cards and categories.
 */
fun bucketColorFor(bucketName: String?): Color {
    if (bucketName.isNullOrBlank()) return Color(0xFF78909C)
    return when (bucketName.trim().lowercase()) {
        "bills" -> Color(0xFF42A5F5)    // Blue
        "reserve" -> Color(0xFF66BB6A)  // Green
        "joint" -> Color(0xFFAB47BC)    // Purple
        "personal" -> Color(0xFFFFA726) // Amber/Orange
        else -> {
            val hash = kotlin.math.abs(bucketName.trim().lowercase().hashCode())
            CustomBucketPalette[hash % CustomBucketPalette.size]
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 1. Top App Bar with the year as its own dropdown title
// ---------------------------------------------------------------------------------------------

/**
 * The title itself is the year selector: "Finner 2026 ▾". Tapping it opens a menu of created years
 * (starting with only the current year) and an "Adicionar novo" item to create the next year.
 */
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
    val nextYear = (availableYears.maxOrNull() ?: selectedYear) + 1

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Finner $selectedYear",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select year",
                modifier = Modifier.size(32.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            availableYears.forEach { year ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Finner $year",
                            fontWeight = if (year == selectedYear) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = {
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
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                text = {
                    Text(
                        text = "Delete year ($selectedYear)",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    expanded = false
                    onDeleteYear(selectedYear)
                }
            )
        }
    }
}

/** Compact title bar (year selector + subtitle). Handles the status-bar inset itself. */
@Composable
fun FinnerTopBar(
    selectedYear: Int,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit,
    onAddNextYear: () -> Unit,
    onDeleteYear: (Int) -> Unit,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------------------------
// 2. Capsule / pill monthly tabs (JAN..DEZ)
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
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
        shape = RoundedCornerShape(50),
        color = if (selected) PillActiveContainer else Color.Transparent,
        border = if (selected) {
            BorderStroke(2.dp, PillActiveBorder)
        } else {
            BorderStroke(1.dp, PillInactiveBorder)
        },
        modifier = Modifier
            .heightIn(min = 44.dp)
            .widthIn(min = 72.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) PillActiveContent else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 3. Compact exchange-rate chip (goes inside the summary card)
// ---------------------------------------------------------------------------------------------

/**
 * Inline clickable badge such as "1 € = R$ 5,50 ✏️".
 * Tapping only calls [onClick]; the caller owns the state that opens the edit field/modal.
 */
@Composable
fun ExchangeRateChip(
    rate: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = String.format(Locale.forLanguageTag("pt-BR"), "1 € = R$ %.2f", rate),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit exchange rate",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 4. Floating auto-hide bottom navigation
// ---------------------------------------------------------------------------------------------

enum class FinnerDestination(val label: String, val icon: ImageVector) {
    MESES("Months", Icons.Default.CalendarMonth),
    OBJETIVOS("Goals", Icons.Default.TrackChanges),
    HISTORICO("History", Icons.Default.History),
    PERFIL("Profile", Icons.Default.Person)
}

/**
 * Tracks scroll direction through nested scrolling: scrolling content down (finger moves up)
 * hides the bar, scrolling up brings it back. Attach [connection] with `Modifier.nestedScroll`.
 */
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
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FinnerDestination.entries.forEach { destination ->
                    FloatingNavItem(
                        destination = destination,
                        selected = destination == selected,
                        onClick = { onSelected(destination) },
                        modifier = Modifier.weight(1f)
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
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = destination.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// 5. Scaffold: auto-hiding floating nav + extended FAB anchored right above it
// ---------------------------------------------------------------------------------------------

private val NavBarSlotHeight = 84.dp
private val FabGap = 12.dp
private val EdgeGap = 16.dp

/**
 * Hosts [content], the floating bottom nav and the extended FAB.
 * The FAB sits just above the nav and slides down to the bottom edge when the nav hides.
 * Bottom content padding of lists should be at least ~170.dp so the last item clears both.
 */
@Composable
fun FinnerFloatingScaffold(
    topBar: @Composable () -> Unit,
    selectedDestination: FinnerDestination,
    onDestinationSelected: (FinnerDestination) -> Unit,
    fabLabel: String?,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val barState = rememberAutoHideBarState()

    // Switching tabs should always reveal the bar again.
    LaunchedEffect(selectedDestination) { barState.show() }

    val fabBottomPadding by animateDpAsState(
        targetValue = if (barState.isVisible) NavBarSlotHeight + FabGap else EdgeGap,
        label = "fabBottomPadding"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(barState.connection),
        topBar = topBar
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()

            if (fabLabel != null) {
                ExtendedFloatingActionButton(
                    onClick = onFabClick,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(fabLabel) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = EdgeGap, bottom = fabBottomPadding)
                )
            }

            FloatingBottomNav(
                selected = selectedDestination,
                onSelected = onDestinationSelected,
                visible = barState.isVisible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = EdgeGap)
            )
        }
    }
}
