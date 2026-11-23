package com.lrds.finner.ui.goals

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import com.lrds.finner.core.currency.CurrencyConverter
import com.lrds.finner.data.local.entity.CurrencyCode
import com.lrds.finner.data.local.entity.GoalEntity
import com.lrds.finner.data.local.entity.GoalPriority
import com.lrds.finner.domain.model.MonthlyDashboardData
import com.lrds.finner.ui.dashboard.MONTH_FULL_NAMES
import com.lrds.finner.ui.dashboard.components.FinnerSectionCard
import com.lrds.finner.ui.dashboard.components.bucketColorFor
import com.lrds.finner.ui.dashboard.components.bucketIconFor
import com.lrds.finner.ui.navigation.FinnerRoute
import com.lrds.finner.ui.theme.FinnerAlpha
import com.lrds.finner.ui.theme.FinnerTheme
import java.time.YearMonth
import kotlin.math.ceil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    selectedYear: Int,
    selectedMonth: Int,
    monthlyDataByMonth: Map<Int, MonthlyDashboardData>,
    onAddGoal: () -> Unit,
    onIncrementGoal: (GoalEntity) -> Unit,
    onEditGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    reselectEvents: Flow<NavKey> = emptyFlow(),
) {
    val listState = rememberLazyListState()
    LaunchedEffect(reselectEvents) {
        reselectEvents.collect { route ->
            if (route == FinnerRoute.Goals) {
                listState.animateScrollToItem(0)
            }
        }
    }

    val activeMonthData = monthlyDataByMonth[selectedMonth]
    val activeRate = activeMonthData?.eurToBrlRate ?: CurrencyConverter.DEFAULT_EUR_TO_BRL_RATE

    val totalTargetEur = remember(goals, activeRate) {
        goals.sumOf { CurrencyConverter.toEurCents(it.targetAmountCents, it.currency, activeRate) }
    }
    val totalSavedEur = remember(goals, activeRate, monthlyDataByMonth) {
        goals.sumOf { goal ->
            val bucketName = goal.bucketName
            val bucketEur = if (!bucketName.isNullOrBlank()) {
                monthlyDataByMonth.values.sumOf { mData ->
                    mData.envelopeSummaries
                        .firstOrNull { it.bucketName.equals(bucketName, ignoreCase = true) }
                        ?.actualEurCents ?: 0L
                }
            } else {
                0L
            }
            CurrencyConverter.toEurCents(goal.currentSavedCents, goal.currency, activeRate) + bucketEur
        }
    }
    val overallProgress = if (totalTargetEur > 0L) {
        (totalSavedEur.toFloat() / totalTargetEur.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val colors = FinnerTheme.colors
    val spacing = FinnerTheme.spacing
    val sizes = FinnerTheme.sizes
    val shapes = FinnerTheme.shapes
    val typography = FinnerTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("screen_goals"),
    ) {
        topBar()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                top = spacing.sm,
                bottom = spacing.floatingNavBottomClearance,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
        ) {
            // Header + "+ New Goal" mint pill
            item(key = "goals_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Financial Goals",
                            style = typography.titleLarge,
                            color = colors.onSurface,
                        )
                        Text(
                            text = "${goals.size} active ${if (goals.size == 1) "target" else "targets"} · ${(overallProgress * 100).toInt()}% completed",
                            style = typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }

                    Surface(
                        onClick = onAddGoal,
                        shape = shapes.pill,
                        color = colors.primary,
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = spacing.pillHorizontal,
                                vertical = spacing.pillVertical,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = colors.onPrimary,
                                modifier = Modifier.size(sizes.iconSm),
                            )
                            Text(
                                text = "New Goal",
                                style = typography.labelLargeBold,
                                color = colors.onPrimary,
                            )
                        }
                    }
                }
            }

            // Total Saved Progress Hero Card
            if (goals.isNotEmpty()) {
                item(key = "goals_summary") {
                    FinnerSectionCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "TOTAL SAVED PROGRESS",
                                style = typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                            Surface(
                                shape = shapes.pill,
                                color = colors.inflowContainer,
                            ) {
                                Text(
                                    text = "${(overallProgress * 100).toInt()}%",
                                    style = typography.labelSmallBold,
                                    color = colors.inflow,
                                    modifier = Modifier.padding(
                                        horizontal = spacing.badgeHorizontal,
                                        vertical = spacing.xs,
                                    ),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(spacing.compactPillVertical))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            Text(
                                text = CurrencyConverter.formatCents(totalSavedEur, CurrencyCode.EUR),
                                style = FinnerTheme.numericStyles.sectionAmount,
                                color = colors.onSurface,
                            )
                            Text(
                                text = "/ ${CurrencyConverter.formatCents(totalTargetEur, CurrencyCode.EUR)}",
                                style = typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = spacing.xxs),
                            )
                        }
                        Spacer(modifier = Modifier.height(spacing.md))
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(sizes.progressBarMd)
                                .clip(shapes.pill),
                            color = colors.primary,
                            trackColor = colors.surfaceContainerHighest,
                        )
                    }
                }
            }

            if (goals.isEmpty()) {
                item(key = "goals_empty") {
                    FinnerSectionCard {
                        Column(verticalArrangement = Arrangement.spacedBy(spacing.compactPillVertical)) {
                            Text(
                                text = "No goals registered yet.",
                                style = typography.titleSmallBold,
                                color = colors.onSurface,
                            )
                            Text(
                                text = "Tap '+ New Goal' above to create financial targets and link them to a bucket for automatic pace forecasts.",
                                style = typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    var menuExpanded by remember { mutableStateOf(false) }
                    val bucketName = goal.bucketName
                    val hasBucket = !bucketName.isNullOrBlank()
                    val bucketColor = if (hasBucket) bucketColorFor(bucketName) else colors.primary

                    val bucketYearlyActualEurCents = if (hasBucket) {
                        monthlyDataByMonth.values.sumOf { mData ->
                            mData.envelopeSummaries
                                .firstOrNull { it.bucketName.equals(bucketName, ignoreCase = true) }
                                ?.actualEurCents ?: 0L
                        }
                    } else {
                        0L
                    }
                    val bucketSavedInGoalCurrency = if (hasBucket) {
                        when (goal.currency) {
                            CurrencyCode.EUR -> bucketYearlyActualEurCents
                            CurrencyCode.BRL -> CurrencyConverter.eurCentsToBrlCents(bucketYearlyActualEurCents, activeRate)
                        }
                    } else {
                        0L
                    }

                    val activeBucketEnvelope = if (hasBucket) {
                        activeMonthData?.envelopeSummaries?.firstOrNull {
                            it.bucketName.equals(bucketName, ignoreCase = true)
                        }
                    } else {
                        null
                    }
                    val monthlyBucketPaceEurCents = if (hasBucket) {
                        val activeMonthPace = when {
                            (activeBucketEnvelope?.actualEurCents ?: 0L) > 0L -> activeBucketEnvelope!!.actualEurCents
                            (activeBucketEnvelope?.plannedEurCents ?: 0L) > 0L -> activeBucketEnvelope!!.plannedEurCents
                            else -> 0L
                        }
                        if (activeMonthPace > 0L) {
                            activeMonthPace
                        } else {
                            val nonZeroMonths = monthlyDataByMonth.values.mapNotNull { mData ->
                                val env = mData.envelopeSummaries.firstOrNull {
                                    it.bucketName.equals(bucketName, ignoreCase = true)
                                }
                                val v = if ((env?.actualEurCents ?: 0L) > 0L) env!!.actualEurCents else (env?.plannedEurCents ?: 0L)
                                v.takeIf { it > 0L }
                            }
                            if (nonZeroMonths.isNotEmpty()) nonZeroMonths.average().toLong() else 0L
                        }
                    } else {
                        0L
                    }
                    val monthlyPaceInGoalCurrency = if (hasBucket) {
                        when (goal.currency) {
                            CurrencyCode.EUR -> monthlyBucketPaceEurCents
                            CurrencyCode.BRL -> CurrencyConverter.eurCentsToBrlCents(monthlyBucketPaceEurCents, activeRate)
                        }
                    } else {
                        0L
                    }

                    val effectiveSavedCents = goal.currentSavedCents + bucketSavedInGoalCurrency
                    val effectiveProgress = if (goal.targetAmountCents <= 0L) {
                        0f
                    } else {
                        (effectiveSavedCents.toFloat() / goal.targetAmountCents.toFloat()).coerceIn(0f, 1f)
                    }
                    val remainingCents = (goal.targetAmountCents - effectiveSavedCents).coerceAtLeast(0L)

                    val forecastText = when {
                        remainingCents <= 0L -> "Goal reached!"
                        hasBucket && monthlyPaceInGoalCurrency > 0L -> {
                            val monthsNeeded = ceil(remainingCents.toDouble() / monthlyPaceInGoalCurrency.toDouble()).toLong().coerceAtLeast(1L)
                            val targetYearMonth = YearMonth.of(selectedYear, selectedMonth).plusMonths(monthsNeeded)
                            val targetMonthName = MONTH_FULL_NAMES[(targetYearMonth.monthValue - 1).coerceIn(0, 11)]
                            "Est. $targetMonthName ${targetYearMonth.year} (~$monthsNeeded mo at ${CurrencyConverter.formatCents(monthlyPaceInGoalCurrency, goal.currency)}/mo)"
                        }
                        hasBucket -> "Log contributions in '$bucketName' to forecast completion."
                        else -> null
                    }

                    FinnerSectionCard(contentPadding = PaddingValues(spacing.cardPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.md),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(sizes.iconBadgeMd)
                                    .clip(shapes.pill)
                                    .background(bucketColor.copy(alpha = FinnerAlpha.tintedContainer)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (hasBucket) bucketIconFor(bucketName) else Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = bucketColor,
                                    modifier = Modifier.size(sizes.iconMd),
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goal.title,
                                    style = typography.titleSmallBold,
                                    color = colors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.compactPillVertical),
                                ) {
                                    if (hasBucket) {
                                        Text(
                                            text = bucketName!!,
                                            style = typography.labelSmall,
                                            color = bucketColor,
                                        )
                                        Text(
                                            text = "·",
                                            style = typography.labelSmall,
                                            color = colors.onSurfaceVariant,
                                        )
                                    }
                                    Text(
                                        text = goal.priority.label,
                                        style = typography.labelSmall,
                                        color = colors.onSurfaceVariant,
                                    )
                                }
                            }

                            Surface(
                                onClick = { onIncrementGoal(goal) },
                                shape = shapes.pill,
                                color = colors.surfaceContainerHigh,
                                border = BorderStroke(sizes.borderThin, colors.cardBorder),
                            ) {
                                Text(
                                    text = "+100 ${goal.currency.symbol}",
                                    style = typography.labelSmall,
                                    color = colors.primary,
                                    modifier = Modifier.padding(
                                        horizontal = spacing.compactPillHorizontal,
                                        vertical = spacing.compactPillVertical,
                                    ),
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(sizes.iconButtonSm),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "Goal actions",
                                        tint = colors.onSurfaceVariant,
                                        modifier = Modifier.size(sizes.iconMd),
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false },
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(sizes.iconMd),
                                            )
                                        },
                                        text = { Text("Edit goal") },
                                        onClick = {
                                            menuExpanded = false
                                            onEditGoal(goal)
                                        },
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = null,
                                                tint = colors.error,
                                                modifier = Modifier.size(sizes.iconMd),
                                            )
                                        },
                                        text = { Text("Delete goal", color = colors.error) },
                                        onClick = {
                                            menuExpanded = false
                                            onDeleteGoal(goal)
                                        },
                                    )
                                }
                            }
                        }

                        if (goal.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(spacing.compactPillVertical))
                            Text(
                                text = goal.notes,
                                style = typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.md))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${CurrencyConverter.formatCents(effectiveSavedCents, goal.currency)} / ${CurrencyConverter.formatCents(goal.targetAmountCents, goal.currency)}",
                                style = FinnerTheme.numericStyles.rowAmount,
                                color = colors.onSurface,
                            )
                            Text(
                                text = "${(effectiveProgress * 100).toInt()}%",
                                style = typography.labelMediumBold,
                                color = bucketColor,
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.compactPillVertical))
                        LinearProgressIndicator(
                            progress = { effectiveProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(sizes.progressBarMd)
                                .clip(shapes.pill),
                            color = bucketColor,
                            trackColor = colors.surfaceContainerHighest,
                        )

                        if (forecastText != null) {
                            Spacer(modifier = Modifier.height(spacing.sm))
                            Text(
                                text = forecastText,
                                style = typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Goals Light", showBackground = true)
@Preview(name = "Goals Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun GoalsScreenPreview() {
    FinnerTheme {
        GoalsScreen(
            goals = listOf(
                GoalEntity(
                    id = "1",
                    title = "Emergency Reserve",
                    targetAmountCents = 15_000_00L,
                    currentSavedCents = 9_200_00L,
                    currency = CurrencyCode.EUR,
                    priority = GoalPriority.LONG_TERM,
                    bucketName = "Reserve",
                    notes = "6 months of essential household expenses",
                ),
            ),
            selectedYear = 2026,
            selectedMonth = 11,
            monthlyDataByMonth = emptyMap(),
            onAddGoal = {},
            onIncrementGoal = {},
            onEditGoal = {},
            onDeleteGoal = {},
        )
    }
}
