package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = OnElectricCyan,
    primaryContainer = ElectricCyanContainer,
    onPrimaryContainer = ElectricCyan,
    secondary = WarmAmber,
    onSecondary = OnWarmAmber,
    secondaryContainer = WarmAmberContainer,
    onSecondaryContainer = WarmAmber,
    tertiary = EmeraldMint,
    onTertiary = ObsidianBackground,
    tertiaryContainer = EmeraldMintContainer,
    onTertiaryContainer = EmeraldMint,
    error = CoralRed,
    onError = OnCoralRed,
    errorContainer = CoralRedContainer,
    onErrorContainer = CoralRed,
    background = ObsidianBackground,
    onBackground = TextPrimaryWhite,
    surface = ObsidianSurface,
    onSurface = TextPrimaryWhite,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = TextSecondarySlate,
    outline = ObsidianDisplayBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = ObsidianDarkColorScheme,
        typography = Typography,
        content = content
    )
}
