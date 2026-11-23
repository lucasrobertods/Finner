package com.lrds.finner.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

/**
 * Cohesive Material 3 motion specs for Finner's Navigation 3 transitions:
 * - Bottom-bar tab switches: subtle directional Shared-Axis X + Fade-Through.
 * - Push/pop drill-downs (e.g. MonthDetails): horizontal slide + parallax + fade.
 * - Predictive back: 1:1 linear-eased scrub transitions for detail pops and tab back gestures.
 */
object FinnerTransitions {

    private const val META_TAB_INDEX = "finner.transition.tabIndex"
    private const val META_IS_DETAIL = "finner.transition.isDetail"

    private const val TAB_ENTER_MS = 240
    private const val TAB_EXIT_MS = 160
    private const val DETAIL_DURATION_MS = 320
    private const val PREDICTIVE_DURATION_MS = 280

    private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    private val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    fun tabMetadata(tabIndex: Int): Map<String, Any> = mapOf(
        META_TAB_INDEX to tabIndex,
        META_IS_DETAIL to false
    )

    fun detailMetadata(): Map<String, Any> = mapOf(
        META_IS_DETAIL to true
    )

    val forwardTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        val initialMeta = initialState.entries.lastOrNull()?.metadata.orEmpty()
        val targetMeta = targetState.entries.lastOrNull()?.metadata.orEmpty()
        val initialIsDetail = initialMeta[META_IS_DETAIL] as? Boolean ?: false
        val targetIsDetail = targetMeta[META_IS_DETAIL] as? Boolean ?: false

        when {
            targetIsDetail -> detailPushTransform()
            initialIsDetail && !targetIsDetail -> detailPopTransform()
            else -> {
                val initialIndex = initialMeta[META_TAB_INDEX] as? Int ?: 0
                val targetIndex = targetMeta[META_TAB_INDEX] as? Int ?: 0
                tabSwitchTransform(forward = targetIndex >= initialIndex)
            }
        }
    }

    val popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        val initialMeta = initialState.entries.lastOrNull()?.metadata.orEmpty()
        val targetMeta = targetState.entries.lastOrNull()?.metadata.orEmpty()
        val initialIsDetail = initialMeta[META_IS_DETAIL] as? Boolean ?: false
        val targetIsDetail = targetMeta[META_IS_DETAIL] as? Boolean ?: false

        when {
            initialIsDetail -> detailPopTransform()
            targetIsDetail -> detailPushTransform()
            else -> {
                val initialIndex = initialMeta[META_TAB_INDEX] as? Int ?: 0
                val targetIndex = targetMeta[META_TAB_INDEX] as? Int ?: 0
                tabSwitchTransform(forward = targetIndex >= initialIndex)
            }
        }
    }

    val predictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = { _ ->
        val initialMeta = initialState.entries.lastOrNull()?.metadata.orEmpty()
        val initialIsDetail = initialMeta[META_IS_DETAIL] as? Boolean ?: false

        if (initialIsDetail) {
            predictiveDetailPopTransform()
        } else {
            predictiveTabPopTransform()
        }
    }

    private fun tabSwitchTransform(forward: Boolean): ContentTransform {
        val direction = if (forward) 1 else -1
        val enter = fadeIn(
            animationSpec = tween(
                durationMillis = TAB_ENTER_MS,
                delayMillis = 40,
                easing = StandardEasing
            )
        ) + slideInHorizontally(
            animationSpec = tween(
                durationMillis = TAB_ENTER_MS,
                easing = EmphasizedDecelerate
            ),
            initialOffsetX = { fullWidth -> (fullWidth * 0.05f * direction).toInt() }
        )
        val exit = fadeOut(
            animationSpec = tween(
                durationMillis = TAB_EXIT_MS,
                easing = StandardEasing
            )
        ) + slideOutHorizontally(
            animationSpec = tween(
                durationMillis = TAB_EXIT_MS,
                easing = EmphasizedAccelerate
            ),
            targetOffsetX = { fullWidth -> (-fullWidth * 0.05f * direction).toInt() }
        )
        return enter togetherWith exit
    }

    private fun detailPushTransform(): ContentTransform {
        val enter = slideInHorizontally(
            animationSpec = tween(
                durationMillis = DETAIL_DURATION_MS,
                easing = EmphasizedDecelerate
            ),
            initialOffsetX = { fullWidth -> fullWidth }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = DETAIL_DURATION_MS / 2,
                easing = StandardEasing
            ),
            initialAlpha = 0.85f
        )
        val exit = slideOutHorizontally(
            animationSpec = tween(
                durationMillis = DETAIL_DURATION_MS,
                easing = EmphasizedDecelerate
            ),
            targetOffsetX = { fullWidth -> -fullWidth / 4 }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = DETAIL_DURATION_MS,
                easing = StandardEasing
            ),
            targetAlpha = 0.85f
        )
        return ContentTransform(
            targetContentEnter = enter,
            initialContentExit = exit,
            targetContentZIndex = 1f
        )
    }

    private fun detailPopTransform(): ContentTransform {
        val enter = slideInHorizontally(
            animationSpec = tween(
                durationMillis = DETAIL_DURATION_MS,
                easing = EmphasizedDecelerate
            ),
            initialOffsetX = { fullWidth -> -fullWidth / 4 }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = DETAIL_DURATION_MS,
                easing = StandardEasing
            ),
            initialAlpha = 0.85f
        )
        val exit = slideOutHorizontally(
            animationSpec = tween(
                durationMillis = DETAIL_DURATION_MS,
                easing = EmphasizedDecelerate
            ),
            targetOffsetX = { fullWidth -> fullWidth }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = DETAIL_DURATION_MS,
                easing = StandardEasing
            )
        )
        return ContentTransform(
            targetContentEnter = enter,
            initialContentExit = exit,
            targetContentZIndex = -1f
        )
    }

    private fun predictiveDetailPopTransform(): ContentTransform {
        val enter = slideInHorizontally(
            animationSpec = tween(
                durationMillis = PREDICTIVE_DURATION_MS,
                easing = LinearEasing
            ),
            initialOffsetX = { fullWidth -> -fullWidth / 4 }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = PREDICTIVE_DURATION_MS,
                easing = LinearEasing
            ),
            initialAlpha = 0.85f
        )
        val exit = slideOutHorizontally(
            animationSpec = tween(
                durationMillis = PREDICTIVE_DURATION_MS,
                easing = LinearEasing
            ),
            targetOffsetX = { fullWidth -> fullWidth }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = PREDICTIVE_DURATION_MS,
                easing = LinearEasing
            ),
            targetAlpha = 0.35f
        )
        return ContentTransform(
            targetContentEnter = enter,
            initialContentExit = exit,
            targetContentZIndex = -1f
        )
    }

    private fun predictiveTabPopTransform(): ContentTransform {
        val enter = fadeIn(
            animationSpec = tween(
                durationMillis = PREDICTIVE_DURATION_MS,
                easing = LinearEasing
            )
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = PREDICTIVE_DURATION_MS,
                easing = LinearEasing
            ),
            initialScale = 0.96f
        )
        val exit = fadeOut(
            animationSpec = tween(
                durationMillis = PREDICTIVE_DURATION_MS,
                easing = LinearEasing
            )
        ) + scaleOut(
            animationSpec = tween(
                durationMillis = PREDICTIVE_DURATION_MS,
                easing = LinearEasing
            ),
            targetScale = 0.92f
        )
        return ContentTransform(
            targetContentEnter = enter,
            initialContentExit = exit,
            targetContentZIndex = -1f
        )
    }
}
