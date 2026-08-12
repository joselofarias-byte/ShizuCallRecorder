/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Violet80,
    onPrimary = DeepDarkViolet,
    primaryContainer = VioletContainerDark,
    onPrimaryContainer = VioletContainerLight,

    secondary = VioletGrey80,
    onSecondary = DarkGreyViolet,

    tertiary = AccentViolet80,
    onTertiary = AccentVioletDark,

    surface = DarkSurface,
    onSurface = OffWhiteText,
    outline = GreyVioletOutline
)

private val LightColorScheme = lightColorScheme(
    primary = Violet40,
    onPrimary = White,
    primaryContainer = VioletContainerLight,
    onPrimaryContainer = VeryDarkViolet,

    secondary = VioletGrey40,
    onSecondary = White,

    tertiary = AccentVioletDark,
    onTertiary = White,

    surface = LightSurface,
    onSurface = NearBlackText,
    outline = GreyVioletOutline
)

@Composable
fun ShizucallrecorderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color remains available as an explicit user option on Android 12+.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
