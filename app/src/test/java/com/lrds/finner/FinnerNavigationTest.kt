package com.lrds.finner

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.lrds.finner.data.local.entity.TransactionType
import com.lrds.finner.ui.navigation.FinnerRoute
import com.lrds.finner.ui.navigation.NavigationState
import com.lrds.finner.ui.navigation.Navigator
import com.lrds.finner.ui.navigation.TOP_LEVEL_ROUTES
import com.lrds.finner.ui.navigation.isTopLevelRoute
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinnerNavigationTest {

    private fun createNavigator(): Pair<NavigationState, Navigator> {
        val state = NavigationState(
            startRoute = FinnerRoute.Dashboard,
            topLevelRoute = mutableStateOf<NavKey>(FinnerRoute.Dashboard),
            backStacks = TOP_LEVEL_ROUTES.associateWith { key -> NavBackStack(key) }
        )
        return state to Navigator(state)
    }

    @Test
    fun `initial state starts on Dashboard with single stack in use`() {
        val (state, navigator) = createNavigator()

        assertEquals(FinnerRoute.Dashboard, state.topLevelRoute)
        assertEquals(FinnerRoute.Dashboard, state.currentRoute)
        assertEquals(listOf<NavKey>(FinnerRoute.Dashboard), state.stacksInUse)
        assertFalse(navigator.canGoBack)
        assertTrue(state.currentRoute.isTopLevelRoute())
    }

    @Test
    fun `switching top level tabs retains start route at base and exits through home`() {
        val (state, navigator) = createNavigator()

        navigator.navigate(FinnerRoute.Goals)
        assertEquals(FinnerRoute.Goals, state.topLevelRoute)
        assertEquals(FinnerRoute.Goals, state.currentRoute)
        assertEquals(listOf<NavKey>(FinnerRoute.Dashboard, FinnerRoute.Goals), state.stacksInUse)
        assertTrue(navigator.canGoBack)

        navigator.navigate(FinnerRoute.History)
        assertEquals(FinnerRoute.History, state.topLevelRoute)
        assertEquals(listOf<NavKey>(FinnerRoute.Dashboard, FinnerRoute.History), state.stacksInUse)

        // Back from a non-start tab returns directly to Dashboard
        navigator.goBack()
        assertEquals(FinnerRoute.Dashboard, state.topLevelRoute)
        assertEquals(listOf<NavKey>(FinnerRoute.Dashboard), state.stacksInUse)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun `pushing MonthDetails onto Dashboard keeps per-tab back stacks independent`() {
        val (state, navigator) = createNavigator()
        val details = FinnerRoute.MonthDetails(
            year = 2026,
            month = 11,
            initialType = TransactionType.EXPENSE_LOCAL,
            initialBucket = "Bills"
        )

        navigator.navigate(details)
        assertEquals(details, state.currentRoute)
        assertFalse(state.currentRoute.isTopLevelRoute())
        assertEquals(2, state.backStacks[FinnerRoute.Dashboard]?.size)
        assertTrue(navigator.canGoBack)
        assertEquals(TransactionType.EXPENSE_LOCAL, details.toFilter().initialType)
        assertEquals("Bills", details.toFilter().initialBucket)

        // Switch to History and open a different month detail
        navigator.navigate(FinnerRoute.History)
        val historyDetails = FinnerRoute.MonthDetails(year = 2026, month = 10)
        navigator.navigate(historyDetails)
        assertEquals(historyDetails, state.currentRoute)

        // Switching back to Dashboard restores Dashboard's MonthDetails stack intact
        navigator.navigate(FinnerRoute.Dashboard)
        assertEquals(details, state.currentRoute)

        // Pop returns to Dashboard root
        navigator.goBack()
        assertEquals(FinnerRoute.Dashboard, state.currentRoute)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun `duplicate consecutive detail push is ignored`() {
        val (state, navigator) = createNavigator()
        val details = FinnerRoute.MonthDetails(year = 2026, month = 11)

        navigator.navigate(details)
        navigator.navigate(details)

        assertEquals(2, state.backStacks[FinnerRoute.Dashboard]?.size)
    }

    @Test
    fun `reselecting active tab pops sub-routes to root and then emits reselect event`() = runTest {
        val (state, navigator) = createNavigator()
        val details = FinnerRoute.MonthDetails(year = 2026, month = 11)

        navigator.navigate(details)
        assertEquals(details, state.currentRoute)

        // First reselect pops back to Dashboard root
        navigator.navigate(FinnerRoute.Dashboard)
        assertEquals(FinnerRoute.Dashboard, state.currentRoute)
        assertEquals(1, state.backStacks[FinnerRoute.Dashboard]?.size)

        // Second reselect while already at root emits reselect event for scroll-to-top
        var emitted: NavKey? = null
        val job = launch { emitted = navigator.reselectEvents.first() }
        testScheduler.runCurrent()

        navigator.navigate(FinnerRoute.Dashboard)
        testScheduler.advanceUntilIdle()
        job.join()

        assertEquals(FinnerRoute.Dashboard, emitted)
    }
}
