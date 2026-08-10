package com.anhprgm.deviceinfo.navigation

import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry

private const val DURATION = 300
private const val FADE_OUT_DURATION = 150

/**
 * Whether the system animation scale is zero.
 *
 * Compose does not honour "Remove animations" / animator duration scale 0 for
 * navigation transitions on its own, and that is a common accessibility
 * setting — people who get motion sickness turn it on.
 */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        val scale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )
        scale != 0f
    }
}

/**
 * Lateral slide for drilling into a detail screen. Siblings within a tab.
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.forwardEnter(enabled: Boolean) =
    if (!enabled) EnterTransition.None
    else slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        tween(DURATION)
    ) + fadeIn(tween(DURATION))

fun AnimatedContentTransitionScope<NavBackStackEntry>.forwardExit(enabled: Boolean) =
    if (!enabled) ExitTransition.None
    else slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Start,
        tween(DURATION)
    ) + fadeOut(tween(FADE_OUT_DURATION))

fun AnimatedContentTransitionScope<NavBackStackEntry>.backEnter(enabled: Boolean) =
    if (!enabled) EnterTransition.None
    else slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        tween(DURATION)
    ) + fadeIn(tween(DURATION))

fun AnimatedContentTransitionScope<NavBackStackEntry>.backExit(enabled: Boolean) =
    if (!enabled) ExitTransition.None
    else slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.End,
        tween(DURATION)
    ) + fadeOut(tween(FADE_OUT_DURATION))

/**
 * Tab switches cross-fade. A lateral slide between top-level tabs reads as a
 * hierarchy change, which is the wrong signal — they are peers.
 */
fun tabEnter(enabled: Boolean): EnterTransition =
    if (!enabled) EnterTransition.None else fadeIn(tween(DURATION))

fun tabExit(enabled: Boolean): ExitTransition =
    if (!enabled) ExitTransition.None else fadeOut(tween(DURATION))
