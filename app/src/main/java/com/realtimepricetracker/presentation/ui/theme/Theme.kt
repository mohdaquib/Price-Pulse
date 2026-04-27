package com.realtimepricetracker.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TradingDarkColorScheme = darkColorScheme(
    primary                = AccentGold,
    onPrimary              = Color(0xFF1A1200),
    primaryContainer       = Color(0xFF3D2F00),
    onPrimaryContainer     = AccentGold,
    secondary              = TradingMuted,
    onSecondary            = TradingBg,
    secondaryContainer     = TradingSurfaceVariant,
    onSecondaryContainer   = TradingOnBg,
    tertiary               = AccentBlue,
    onTertiary             = Color(0xFF001030),
    background             = TradingBg,
    onBackground           = TradingOnBg,
    surface                = TradingSurface,
    onSurface              = TradingOnSurface,
    surfaceVariant         = TradingSurfaceVariant,
    onSurfaceVariant       = TradingMuted,
    outline                = TradingSurfaceVariant,
    outlineVariant         = TradingSurfaceVariant,
    inverseSurface         = TradingOnBg,
    inverseOnSurface       = TradingBg,
    error                  = BearRed,
    onError                = Color(0xFF1A0000),
    errorContainer         = BearRed.copy(alpha = 0.15f),
    onErrorContainer       = BearRed,
)

// Minimal light scheme — user can toggle; not the primary experience
private val TradingLightColorScheme = lightColorScheme(
    primary            = Color(0xFF6650A4),
    background         = LightBackground,
    surface            = LightSurface,
    onBackground       = Color(0xFF1C1B1F),
    onSurface          = Color(0xFF1C1B1F),
    onSurfaceVariant   = Color(0xFF49454F),
    error              = BearRed,
)

@Composable
fun RealtimePriceTrackerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    // Dynamic colour (Material You) is intentionally disabled — it overrides the
    // trading colour palette which is the core of the visual identity.
    val colorScheme = if (darkTheme) TradingDarkColorScheme else TradingLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
