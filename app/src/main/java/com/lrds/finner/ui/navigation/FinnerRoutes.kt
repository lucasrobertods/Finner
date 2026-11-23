package com.lrds.finner.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.ui.dashboard.MonthDetailsFilter
import kotlinx.serialization.Serializable

/**
 * Type-safe, serializable Navigation 3 route keys for Finner.
 */
@Serializable
sealed interface FinnerRoute : NavKey {

    @Serializable
    data object Dashboard : FinnerRoute

    @Serializable
    data class MonthDetails(
        val year: Int,
        val month: Int,
        val initialType: TransactionType? = null,
        val initialBucket: String? = null
    ) : FinnerRoute {
        fun toFilter(): MonthDetailsFilter = MonthDetailsFilter(
            initialType = initialType,
            initialBucket = initialBucket
        )
    }

    @Serializable
    data object Goals : FinnerRoute

    @Serializable
    data object History : FinnerRoute

    @Serializable
    data object Profile : FinnerRoute
}

/**
 * Metadata for primary bottom-bar destinations in Finner.
 */
data class TopLevelDestination(
    val route: FinnerRoute,
    val label: String,
    val icon: ImageVector,
    val tabIndex: Int
)

val TOP_LEVEL_DESTINATIONS: List<TopLevelDestination> = listOf(
    TopLevelDestination(
        route = FinnerRoute.Dashboard,
        label = "Months",
        icon = Icons.Default.CalendarMonth,
        tabIndex = 0
    ),
    TopLevelDestination(
        route = FinnerRoute.Goals,
        label = "Goals",
        icon = Icons.Default.TrackChanges,
        tabIndex = 1
    ),
    TopLevelDestination(
        route = FinnerRoute.History,
        label = "History",
        icon = Icons.Default.History,
        tabIndex = 2
    ),
    TopLevelDestination(
        route = FinnerRoute.Profile,
        label = "Profile",
        icon = Icons.Default.Person,
        tabIndex = 3
    )
)

val TOP_LEVEL_ROUTES: Set<NavKey> = TOP_LEVEL_DESTINATIONS.mapTo(linkedSetOf()) { it.route }

fun NavKey.isTopLevelRoute(): Boolean = this in TOP_LEVEL_ROUTES
