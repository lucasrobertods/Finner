package com.lrds.finner.ui.profile

import android.content.res.Configuration
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey
import com.lrds.finner.ui.dashboard.components.FinnerSectionCard
import com.lrds.finner.ui.history.FinnerFilterPill
import com.lrds.finner.ui.navigation.FinnerRoute
import com.lrds.finner.ui.theme.FinnerAlpha
import com.lrds.finner.ui.theme.FinnerTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ProfileScreen(
    selectedYear: Int,
    userDisplayName: String,
    userEmail: String?,
    householdName: String,
    householdId: String,
    isCloudConnected: Boolean,
    selectedUserFilter: String?,
    currentUserId: String,
    onUserFilterChange: (String?) -> Unit,
    onManageAccount: () -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    reselectEvents: Flow<NavKey> = emptyFlow(),
) {
    val listState = rememberLazyListState()
    LaunchedEffect(reselectEvents) {
        reselectEvents.collect { route ->
            if (route == FinnerRoute.Profile) {
                listState.animateScrollToItem(0)
            }
        }
    }

    val initials = remember(userDisplayName) {
        userDisplayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "F" }
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
            .testTag("screen_profile"),
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
            item(key = "profile_header") {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                    Text(
                        text = "Profile & Settings",
                        style = typography.titleLarge,
                        color = colors.onSurface,
                    )
                    Text(
                        text = "Account & Household Workspace",
                        style = typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }

            // Household Identity Card + 3-column summary strip
            item(key = "profile_identity") {
                FinnerSectionCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(sizes.avatarLg)
                                .clip(shapes.pill)
                                .background(colors.primary.copy(alpha = FinnerAlpha.selectedContainer)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = initials,
                                style = typography.titleMediumBold,
                                color = colors.primary,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(spacing.xs),
                        ) {
                            Text(
                                text = householdName,
                                style = typography.titleMediumBold,
                                color = colors.onSurface,
                            )
                            Text(
                                text = userEmail ?: userDisplayName,
                                style = typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Surface(
                            shape = shapes.pill,
                            color = if (isCloudConnected) {
                                colors.inflowContainer
                            } else {
                                colors.surfaceContainerHighest
                            },
                        ) {
                            Text(
                                text = if (isCloudConnected) "Synced" else "Offline",
                                style = typography.labelSmallBold,
                                color = if (isCloudConnected) {
                                    colors.inflow
                                } else {
                                    colors.onSurfaceVariant
                                },
                                modifier = Modifier.padding(
                                    horizontal = spacing.compactPillHorizontal,
                                    vertical = spacing.xs,
                                ),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.lg))
                    HorizontalDivider(color = colors.cardBorder)
                    Spacer(modifier = Modifier.height(spacing.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        ProfileStatItem(label = "CURRENCY", value = "EUR / BRL")
                        ProfileStatItem(label = "MEMBER", value = userDisplayName)
                        ProfileStatItem(label = "CYCLE", value = "Year $selectedYear")
                    }
                }
            }

            // Data & Sync Card
            item(key = "profile_sync_label") {
                Text(
                    text = "DATA & SYNC",
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = spacing.xs),
                )
            }
            item(key = "profile_sync_card") {
                FinnerSectionCard(onClick = onManageAccount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(sizes.iconBadgeMd)
                                .clip(shapes.pill)
                                .background(colors.primary.copy(alpha = FinnerAlpha.tintedContainer)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(sizes.iconLg),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isCloudConnected) "Cloud Synchronization" else "Offline Mode",
                                style = typography.titleSmall,
                                color = colors.onSurface,
                            )
                            Text(
                                text = "Workspace: $householdId",
                                style = typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            text = "Manage",
                            style = typography.labelLarge,
                            color = colors.primary,
                        )
                    }
                }
            }

            // Entry Visibility Filter Card
            item(key = "profile_filter_label") {
                Text(
                    text = "VIEW ENTRIES FROM",
                    style = typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = spacing.xs),
                )
            }
            item(key = "profile_filter_card") {
                FinnerSectionCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                        FinnerFilterPill(
                            label = "All Members",
                            selected = selectedUserFilter == null,
                            onClick = { onUserFilterChange(null) },
                        )
                        FinnerFilterPill(
                            label = userDisplayName,
                            selected = selectedUserFilter == currentUserId,
                            onClick = { onUserFilterChange(currentUserId) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(FinnerTheme.spacing.xxs)) {
        Text(
            text = label,
            style = FinnerTheme.typography.labelSmall,
            color = FinnerTheme.colors.onSurfaceVariant,
        )
        Text(
            text = value,
            style = FinnerTheme.typography.bodySmallSemiBold,
            color = FinnerTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Profile Light", showBackground = true)
@Preview(name = "Profile Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenPreview() {
    FinnerTheme {
        ProfileScreen(
            selectedYear = 2026,
            userDisplayName = "Lucas Roberto",
            userEmail = "lucas@finner.app",
            householdName = "Household Ledger",
            householdId = "household-default",
            isCloudConnected = true,
            selectedUserFilter = null,
            currentUserId = "u1",
            onUserFilterChange = {},
            onManageAccount = {},
        )
    }
}
