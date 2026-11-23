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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.lrds.finner.ui.theme.FinnerNumericStyles
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
    reselectEvents: Flow<NavKey> = emptyFlow()
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen_goals")
    ) {
        topBar()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 124.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header + "+ New Goal" mint pill
            item(key = "goals_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Financial Goals",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${goals.size} active ${if (goals.size == 1) "target" else "targets"} · ${(overallProgress * 100).toInt()}% completed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        onClick = onAddGoal,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "New Goal",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL SAVED PROGRESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = CircleShape,
                                color = FinnerTheme.extendedColors.inflowContainer
                            ) {
                                Text(
                                    text = "${(overallProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = FinnerTheme.extendedColors.inflow,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = CurrencyConverter.formatCents(totalSavedEur, CurrencyCode.EUR),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "/ ${CurrencyConverter.formatCents(totalTargetEur, CurrencyCode.EUR)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }
            }

            if (goals.isEmpty()) {
                item(key = "goals_empty") {
                    FinnerSectionCard {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "No goals registered yet.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap '+ New Goal' above to create financial targets and link them to a bucket for automatic pace forecasts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    var menuExpanded by remember { mutableStateOf(false) }
                    val bucketName = goal.bucketName
                    val hasBucket = !bucketName.isNullOrBlank()
                    val bucketColor = if (hasBucket) bucketColorFor(bucketName) else MaterialTheme.colorScheme.primary

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

                    FinnerSectionCard(contentPadding = PaddingValues(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(bucketColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasBucket) bucketIconFor(bucketName) else Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = bucketColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (hasBucket) {
                                        Text(
                                            text = bucketName!!,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = bucketColor
                                        )
                                        Text(
                                            text = "·",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = goal.priority.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                onClick = { onIncrementGoal(goal) },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, FinnerTheme.extendedColors.cardBorder)
                            ) {
                                Text(
                                    text = "+100 ${goal.currency.symbol}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "Goal actions",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                        text = { Text("Edit goal") },
                                        onClick = {
                                            menuExpanded = false
                                            onEditGoal(goal)
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        text = { Text("Delete goal", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            menuExpanded = false
                                            onDeleteGoal(goal)
                                        }
                                    )
                                }
                            }
                        }

                        if (goal.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = goal.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${CurrencyConverter.formatCents(effectiveSavedCents, goal.currency)} / ${CurrencyConverter.formatCents(goal.targetAmountCents, goal.currency)}",
                                style = FinnerNumericStyles.rowAmount,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(effectiveProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = bucketColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { effectiveProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = bucketColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        if (forecastText != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = forecastText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    notes = "6 months of essential household expenses"
                )
            ),
            selectedYear = 2026,
            selectedMonth = 11,
            monthlyDataByMonth = emptyMap(),
            onAddGoal = {},
            onIncrementGoal = {},
            onEditGoal = {},
            onDeleteGoal = {}
        )
    }
}
