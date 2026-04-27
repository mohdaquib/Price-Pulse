package com.realtimepricetracker.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// ── Core trading palette (Binance-inspired) ──────────────────────────────────

val TradingBg             = Color(0xFF0B0E11)   // main screen background
val TradingSurface        = Color(0xFF1E2026)   // cards / bottom sheets
val TradingSurfaceVariant = Color(0xFF2B3139)   // elevated rows, input fields

val TradingOnBg      = Color(0xFFEAECEF)        // primary text on dark bg
val TradingOnSurface = Color(0xFFEAECEF)
val TradingMuted     = Color(0xFF848E9C)        // secondary / label text
val TradingDim       = Color(0xFF474D57)        // tertiary / placeholder text

val BullGreen  = Color(0xFF02C076)              // gains
val BearRed    = Color(0xFFF6465D)              // losses
val AccentGold = Color(0xFFF0B90B)              // selection / accent (Binance yellow)
val AccentBlue = Color(0xFF3773F5)

// ── Semantic aliases (keep existing references compiling) ─────────────────────

val SuccessGreen  = BullGreen
val ErrorRed      = BearRed
val WarningOrange = AccentGold

// ── Legacy colour names (keep previews and old imports compiling) ─────────────

val Purple80        = Color(0xFFD0BCFF)
val PurpleGrey80    = Color(0xFFCCC2DC)
val Pink80          = Color(0xFFEFB8C8)
val Purple40        = Color(0xFF6650a4)
val PurpleGrey40    = Color(0xFF625b71)
val Pink40          = Color(0xFF7D5260)
val DarkBackground  = TradingBg
val LightBackground = Color(0xFFFAFAFA)
val DarkSurface     = TradingSurface
val LightSurface    = Color(0xFFFFFFFF)
