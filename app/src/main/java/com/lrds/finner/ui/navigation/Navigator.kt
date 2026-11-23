package com.lrds.finner.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Handles forward, back, and tab-reselect navigation events by mutating [NavigationState].
 */
class Navigator(val state: NavigationState) {

    private val _reselectEvents = MutableSharedFlow<NavKey>(extraBufferCapacity = 1)
    val reselectEvents: SharedFlow<NavKey> = _reselectEvents.asSharedFlow()

    val canGoBack: Boolean
        get() = state.currentBackStack.size > 1 || state.topLevelRoute != state.startRoute

    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            if (route == state.topLevelRoute) {
                val currentStack = state.currentBackStack
                if (currentStack.size > 1) {
                    while (currentStack.size > 1) {
                        currentStack.removeLastOrNull()
                    }
                } else {
                    _reselectEvents.tryEmit(route)
                }
            } else {
                state.topLevelRoute = route
            }
        } else {
            val currentStack = state.currentBackStack
            if (currentStack.lastOrNull() != route) {
                currentStack.add(route)
            }
        }
    }

    fun goBack() {
        val currentStack = state.currentBackStack
        val currentRoute = currentStack.lastOrNull() ?: return

        if (currentRoute == state.topLevelRoute) {
            if (state.topLevelRoute != state.startRoute) {
                state.topLevelRoute = state.startRoute
            }
        } else {
            currentStack.removeLastOrNull()
        }
    }
}
