package com.lrds.finner.ui.history

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.lrds.finner.data.local.entity.TransactionEntity
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.ui.dashboard.components.FinnerSectionCard
import com.lrds.finner.ui.navigation.FinnerRoute
import com.lrds.finner.ui.theme.FinnerNumericStyles
import com.lrds.finner.ui.theme.FinnerTheme
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun HistoryScreen(
    transactions: List<TransactionEntity>,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    reselectEvents: Flow<NavKey> = emptyFlow()
) {
    val listState = rememberLazyListState()
    LaunchedEffect(reselectEvents) {
        reselectEvents.collect { route ->
            if (route == FinnerRoute.History) {
                listState.animateScrollToItem(0)
            }
        }
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf<TransactionType?>(null) }

    val filteredTransactions = remember(transactions, searchQuery, selectedType) {
        val q = searchQuery.trim().lowercase()
        transactions.filter { tx ->
            val typeMatch = selectedType == null || tx.type == selectedType
            val queryMatch = q.isEmpty() ||
                tx.description.lowercase().contains(q) ||
                tx.userDisplayName.lowercase().contains(q)
            typeMatch && queryMatch
        }
    }

    val groupedByDate = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.date }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen_history")
    ) {
        topBar()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 124.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "history_header") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "History & Logs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredTransactions.size} ${if (filteredTransactions.size == 1) "transaction" else "transactions"} recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Revolut Pill Search Bar
            item(key = "history_search") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search description or member...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = FinnerTheme.extendedColors.cardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Type Filter Pills
            item(key = "history_filters") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FinnerFilterPill(
                            label = "All",
                            selected = selectedType == null,
                            onClick = { selectedType = null }
                        )
                    }
                    item {
                        FinnerFilterPill(
                            label = "Inflows",
                            selected = selectedType == TransactionType.INCOME,
                            onClick = { selectedType = TransactionType.INCOME }
                        )
                    }
                    item {
                        FinnerFilterPill(
                            label = "Outflows",
                            selected = selectedType == TransactionType.EXPENSE_LOCAL,
                            onClick = { selectedType = TransactionType.EXPENSE_LOCAL }
                        )
                    }
                    item {
                        FinnerFilterPill(
                            label = "Brazil",
                            selected = selectedType == TransactionType.EXPENSE_BRAZIL,
                            onClick = { selectedType = TransactionType.EXPENSE_BRAZIL }
                        )
                    }
                }
            }

            if (filteredTransactions.isEmpty()) {
                item(key = "history_empty") {
                    FinnerSectionCard {
                        Text(
                            text = if (transactions.isEmpty()) {
                                "No entries recorded in this year yet."
                            } else {
                                "No transactions match your search or filter."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                groupedByDate.forEach { (date, dayTransactions) ->
                    item(key = "header_$date") {
                        Text(
                            text = date.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                    item(key = "group_$date") {
                        FinnerSectionCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                            dayTransactions.forEachIndexed { idx, tx ->
                                if (idx > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = FinnerTheme.extendedColors.cardBorder
                                    )
                                }
                                HistoryTransactionRow(
                                    transaction = tx,
                                    onEdit = { onEditTransaction(tx) },
                                    onDelete = { onDeleteTransaction(tx) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinnerFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.inverseSurface
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        border = if (selected) null else BorderStroke(1.dp, FinnerTheme.extendedColors.cardBorder),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.inverseOnSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun HistoryTransactionRow(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isIncome = transaction.type == TransactionType.INCOME
    val accentColor = when (transaction.type) {
        TransactionType.INCOME -> FinnerTheme.extendedColors.inflow
        TransactionType.EXPENSE_BRAZIL -> FinnerTheme.extendedColors.brazilAccent
        TransactionType.EXPENSE_LOCAL -> FinnerTheme.extendedColors.outflow
    }
    val icon = when (transaction.type) {
        TransactionType.INCOME -> Icons.Default.ArrowDownward
        TransactionType.EXPENSE_BRAZIL -> Icons.Default.Public
        TransactionType.EXPENSE_LOCAL -> Icons.Default.ArrowUpward
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onEdit)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(17.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${transaction.type.label} · ${transaction.userDisplayName.ifBlank { "Shared" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = (if (isIncome) "+ " else "- ") + CurrencyConverter.formatCents(transaction.amountCents, transaction.currency),
            style = FinnerNumericStyles.rowAmount,
            fontWeight = FontWeight.Bold,
            color = if (isIncome) FinnerTheme.extendedColors.inflow else MaterialTheme.colorScheme.onSurface
        )

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Entry actions",
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
                    text = { Text("Edit entry") },
                    onClick = {
                        menuExpanded = false
                        onEdit()
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
                    text = { Text("Delete entry", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    }
                )
            }
        }
    }
}

@Preview(name = "History Light", showBackground = true)
@Preview(name = "History Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HistoryScreenPreview() {
    FinnerTheme {
        HistoryScreen(
            transactions = listOf(
                TransactionEntity(
                    id = "tx-1",
                    description = "Supermarket Weekly Run",
                    amountCents = 84_50L,
                    currency = CurrencyCode.EUR,
                    date = LocalDate.of(2026, 11, 18),
                    categoryId = "cat-1",
                    type = TransactionType.EXPENSE_LOCAL,
                    userId = "u1",
                    userDisplayName = "Lucas"
                ),
                TransactionEntity(
                    id = "tx-2",
                    description = "Monthly Salary",
                    amountCents = 4_800_00L,
                    currency = CurrencyCode.EUR,
                    date = LocalDate.of(2026, 11, 1),
                    categoryId = "cat-2",
                    type = TransactionType.INCOME,
                    userId = "u1",
                    userDisplayName = "Lucas"
                )
            ),
            onEditTransaction = {},
            onDeleteTransaction = {}
        )
    }
}
