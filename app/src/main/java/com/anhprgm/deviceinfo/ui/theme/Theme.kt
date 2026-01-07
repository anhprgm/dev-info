package com.anhprgm.deviceinfo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Indigo80,
    secondary = Cyan80,
    tertiary = IndigoGrey80,
    background = Grey10,
    surface = Grey20,
    onPrimary = Grey99,
    onSecondary = Grey10,
    onTertiary = Grey10,
    onBackground = Grey90,
    onSurface = Grey90,
)

private val LightColorScheme = lightColorScheme(
    primary = Indigo40,
    secondary = Cyan40,
    tertiary = IndigoGrey30,
    background = Grey99,
    surface = Grey95,
    onPrimary = Grey99,
    onSecondary = Grey99,
    onTertiary = Grey99,
    onBackground = Grey10,
    onSurface = Grey10,
)

@Composable
fun DevInfoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
