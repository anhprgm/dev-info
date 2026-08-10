package com.anhprgm.deviceinfo.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Shared spacing scale. The screens previously mixed 4/6/8/12/16/20/24 dp
 * ad hoc, which is why card gaps did not line up between screens.
 */
object Dimens {
    val spaceXs = 4.dp
    val spaceSm = 8.dp
    val spaceMd = 12.dp
    val spaceLg = 16.dp
    val spaceXl = 24.dp
    val spaceXxl = 32.dp

    /** Standard page gutter. */
    val screenPadding = 16.dp

    val cardPadding = 16.dp
    val cardElevation = 0.dp
    val cardSpacing = 12.dp

    val iconSm = 20.dp
    val iconMd = 24.dp
    val iconLg = 32.dp
    val iconXl = 40.dp

    val gaugeSize = 120.dp
    val gaugeStroke = 12.dp

    val chartHeight = 180.dp
    val sparklineHeight = 48.dp

    /** Minimum touch target per Material accessibility guidance. */
    val minTouchTarget = 48.dp
}
