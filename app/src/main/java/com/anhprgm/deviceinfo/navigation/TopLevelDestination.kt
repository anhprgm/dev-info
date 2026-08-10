package com.anhprgm.deviceinfo.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.ui.graphics.vector.ImageVector
import com.anhprgm.deviceinfo.R
import kotlin.reflect.KClass

/**
 * The bottom-navigation tabs.
 *
 * Settings deliberately is *not* a tab — at ~28 destinations, spending one of
 * four slots on Settings is the classic IA mistake. It lives at the end of
 * Tools and as an action on the dashboard app bar.
 *
 * A TEST tab joins in Phase 4 once the screen/sensor/audio tests exist;
 * registering it now would ship a tab that opens onto nothing.
 */
enum class TopLevelDestination(
    val graph: Any,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    INFO(
        graph = InfoGraph,
        labelRes = R.string.tab_info,
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    ),
    MONITOR(
        graph = MonitorGraph,
        labelRes = R.string.tab_monitor,
        selectedIcon = Icons.Filled.Speed,
        unselectedIcon = Icons.Outlined.Speed
    ),
    TOOLS(
        graph = ToolsGraph,
        labelRes = R.string.tab_tools,
        selectedIcon = Icons.Filled.Build,
        unselectedIcon = Icons.Outlined.Build
    );

    /** Used to test whether the current destination lives under this tab. */
    val graphClass: KClass<*> get() = graph::class
}
