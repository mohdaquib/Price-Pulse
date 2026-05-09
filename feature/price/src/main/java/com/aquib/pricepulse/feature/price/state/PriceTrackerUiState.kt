package com.aquib.pricepulse.feature.price.state

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.aquib.pricepulse.core.common.ui.BaseUiState
import com.aquib.pricepulse.domain.entities.OrderBook
import com.aquib.pricepulse.domain.entities.PriceAlert

enum class AppTab { MARKETS, WATCHLIST }

@Stable
data class StockUiModel(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercentage: Double = 0.0,
    val flashColor: Color? = null,
    val priceHistory: List<Float> = emptyList()
)

@Stable
data class PriceTrackerUiState(
    val stocks: List<StockUiModel> = emptyList(),
    val watchlist: Set<String> = emptySet(),
    val activeTab: AppTab = AppTab.MARKETS,
    val isConnected: Boolean = false,
    val isRunning: Boolean = false,
    val isDarkMode: Boolean = true,
    override val loading: Boolean = false,
    override val error: String? = null,
    val selectedSymbol: String? = null,
    val alerts: List<PriceAlert> = emptyList(),
    val showAlertDialogForSymbol: String? = null,
    val isOffline: Boolean = false,
    val cacheTimestamp: Long? = null,
    val orderBook: OrderBook? = null,
) : BaseUiState
