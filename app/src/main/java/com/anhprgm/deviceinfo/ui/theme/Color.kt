package com.anhprgm.deviceinfo.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Full Material 3 token set, generated from the existing brand seed
 * (Indigo #3F51B5) with a cyan secondary carried over from the old palette.
 *
 * The previous file defined only ~10 roles, so every use of primaryContainer,
 * secondaryContainer, onSurfaceVariant and friends fell back to the M3
 * baseline *purple* — which clashed with the indigo brand. It also set
 * onSecondary to near-white over a light cyan, which fails WCAG contrast.
 */

// ---- Light ----------------------------------------------------------------
val LightPrimary = Color(0xFF3F51B5)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFDFE0FF)
val LightOnPrimaryContainer = Color(0xFF00105E)

val LightSecondary = Color(0xFF006874)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFF97F0FF)
val LightOnSecondaryContainer = Color(0xFF001F24)

val LightTertiary = Color(0xFF4A6267)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFCDE7EC)
val LightOnTertiaryContainer = Color(0xFF051F23)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

val LightBackground = Color(0xFFFBF8FF)
val LightOnBackground = Color(0xFF1B1B21)
val LightSurface = Color(0xFFFBF8FF)
val LightOnSurface = Color(0xFF1B1B21)
val LightSurfaceVariant = Color(0xFFE3E1EC)
val LightOnSurfaceVariant = Color(0xFF46464F)
val LightOutline = Color(0xFF767680)
val LightOutlineVariant = Color(0xFFC7C5D0)
val LightScrim = Color(0xFF000000)
val LightInverseSurface = Color(0xFF303036)
val LightInverseOnSurface = Color(0xFFF3EFF7)
val LightInversePrimary = Color(0xFFBEC2FF)

val LightSurfaceDim = Color(0xFFDBD9E0)
val LightSurfaceBright = Color(0xFFFBF8FF)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF5F2FA)
val LightSurfaceContainer = Color(0xFFEFEDF4)
val LightSurfaceContainerHigh = Color(0xFFE9E7EF)
val LightSurfaceContainerHighest = Color(0xFFE4E1E9)

// ---- Dark -----------------------------------------------------------------
val DarkPrimary = Color(0xFFBEC2FF)
val DarkOnPrimary = Color(0xFF0A1B8F)
val DarkPrimaryContainer = Color(0xFF2536A7)
val DarkOnPrimaryContainer = Color(0xFFDFE0FF)

val DarkSecondary = Color(0xFF4FD8EB)
val DarkOnSecondary = Color(0xFF00363D)
val DarkSecondaryContainer = Color(0xFF004F58)
val DarkOnSecondaryContainer = Color(0xFF97F0FF)

val DarkTertiary = Color(0xFFB1CBD0)
val DarkOnTertiary = Color(0xFF1C3438)
val DarkTertiaryContainer = Color(0xFF334B4F)
val DarkOnTertiaryContainer = Color(0xFFCDE7EC)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkBackground = Color(0xFF131318)
val DarkOnBackground = Color(0xFFE4E1E9)
val DarkSurface = Color(0xFF131318)
val DarkOnSurface = Color(0xFFE4E1E9)
val DarkSurfaceVariant = Color(0xFF46464F)
val DarkOnSurfaceVariant = Color(0xFFC7C5D0)
val DarkOutline = Color(0xFF90909A)
val DarkOutlineVariant = Color(0xFF46464F)
val DarkScrim = Color(0xFF000000)
val DarkInverseSurface = Color(0xFFE4E1E9)
val DarkInverseOnSurface = Color(0xFF303036)
val DarkInversePrimary = Color(0xFF4355B9)

val DarkSurfaceDim = Color(0xFF131318)
val DarkSurfaceBright = Color(0xFF39383F)
val DarkSurfaceContainerLowest = Color(0xFF0E0E13)
val DarkSurfaceContainerLow = Color(0xFF1B1B21)
val DarkSurfaceContainer = Color(0xFF1F1F25)
val DarkSurfaceContainerHigh = Color(0xFF2A2930)
val DarkSurfaceContainerHighest = Color(0xFF35343B)

/**
 * Semantic colours for gauges and status readouts.
 *
 * These are intentionally outside the M3 scheme: "battery is critical" must
 * stay red even when Material You recolours the app to the user's wallpaper.
 */
object StatusColors {
    val good = Color(0xFF2E7D32)
    val goodDark = Color(0xFF81C784)
    val warning = Color(0xFFED6C02)
    val warningDark = Color(0xFFFFB74D)
    val critical = Color(0xFFC62828)
    val criticalDark = Color(0xFFEF9A9A)
}
