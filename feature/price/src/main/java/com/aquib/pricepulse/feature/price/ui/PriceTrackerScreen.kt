package com.aquib.pricepulse.feature.price.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aquib.pricepulse.domain.entities.AlertCondition
import com.aquib.pricepulse.domain.entities.PriceAlert
import com.aquib.pricepulse.feature.price.state.AppTab
import com.aquib.pricepulse.feature.price.state.PriceTrackerUiState
import com.aquib.pricepulse.feature.price.state.StockUiModel
import com.aquib.pricepulse.feature.price.viewmodel.PriceTrackerViewModel
import com.aquib.pricepulse.feature.price.ui.theme.AccentGold
import com.aquib.pricepulse.feature.price.ui.theme.BullGreen
import com.aquib.pricepulse.feature.price.ui.theme.BearRed
import com.aquib.pricepulse.feature.price.ui.theme.PricePulseTheme
import com.aquib.pricepulse.feature.price.ui.theme.WarningOrange

// ── Entry point ───────────────────────────────────────────────────────────────
@Composable
fun PriceTrackerScreen(viewModel: PriceTrackerViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    PricePulseTheme(darkTheme = uiState.isDarkMode) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TradingTopBar(
                    uiState = uiState,
                    onToggleFeed = viewModel::toggleFeed,
                    onToggleDarkMode = viewModel::toggleDarkMode,
                )
            },
        ) { padding ->
            PriceTrackerScreenContent(
                uiState = uiState,
                onStockSelected = viewModel::selectStock,
                onChartDismiss = { viewModel.selectStock(null) },
                onWatchlistToggle = viewModel::toggleWatchlist,
                onTabSelected = viewModel::setActiveTab,
                onShowAlertDialog = viewModel::showAlertDialog,
                onDismissAlertDialog = viewModel::dismissAlertDialog,
                onAddAlert = viewModel::addAlert,
                onRemoveAlert = viewModel::removeAlert,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

// ── Screen content ────────────────────────────────────────────────────────────

@Composable
fun PriceTrackerScreenContent(
    uiState: PriceTrackerUiState,
    onStockSelected: (String) -> Unit = {},
    onChartDismiss: () -> Unit = {},
    onWatchlistToggle: (String) -> Unit = {},
    onTabSelected: (AppTab) -> Unit = {},
    onShowAlertDialog: (String) -> Unit = {},
    onDismissAlertDialog: () -> Unit = {},
    onAddAlert: (symbol: String, price: Double, condition: AlertCondition) -> Unit = { _, _, _ -> },
    onRemoveAlert: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TradingTabRow(activeTab = uiState.activeTab, onTabSelected = onTabSelected)
        OfflineBanner(isOffline = uiState.isOffline, cacheTimestamp = uiState.cacheTimestamp)

        // ── Main content ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                uiState.loading && uiState.stocks.isEmpty() -> LoadingState()
                uiState.error != null                       -> ErrorState(uiState.error)
                else -> {
                    val displayedStocks = when (uiState.activeTab) {
                        AppTab.MARKETS   -> uiState.stocks
                        AppTab.WATCHLIST -> uiState.stocks.filter { it.symbol in uiState.watchlist }
                    }
                    when {
                        displayedStocks.isEmpty() && uiState.activeTab == AppTab.WATCHLIST ->
                            WatchlistEmptyState()

                        displayedStocks.isEmpty() ->
                            EmptyMarketsState()

                        else -> PriceList(
                            stocks = displayedStocks,
                            watchlist = uiState.watchlist,
                            selectedSymbol = uiState.selectedSymbol,
                            onStockClick = onStockSelected,
                            onWatchlistToggle = onWatchlistToggle,
                        )
                    }
                }
            }
        }

        // ── Detail panel (slides in from bottom) ──────────────────────────────
        AnimatedVisibility(
            visible = uiState.selectedSymbol != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            val selectedStock = uiState.stocks.find { it.symbol == uiState.selectedSymbol }
            if (selectedStock != null) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 460.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    StockChartPanel(
                        stock = selectedStock,
                        alerts = uiState.alerts.filter { it.symbol == selectedStock.symbol },
                        onDismiss = onChartDismiss,
                        onSetAlert = { onShowAlertDialog(selectedStock.symbol) },
                        onRemoveAlert = onRemoveAlert,
                    )
                    uiState.orderBook?.let { book ->
                        OrderBookPanel(orderBook = book)
                    }
                }
            }
        }
    }

    // Alert dialog rendered outside the column so it overlays everything
    if (uiState.showAlertDialogForSymbol != null) {
        val price = uiState.stocks
            .find { it.symbol == uiState.showAlertDialogForSymbol }?.price ?: 0.0
        SetAlertDialog(
            symbol = uiState.showAlertDialogForSymbol,
            currentPrice = price,
            onConfirm = { targetPrice, condition ->
                onAddAlert(uiState.showAlertDialogForSymbol, targetPrice, condition)
            },
            onDismiss = onDismissAlertDialog,
        )
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingTopBar(
    uiState: PriceTrackerUiState,
    onToggleFeed: () -> Unit,
    onToggleDarkMode: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Price Pulse",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
        },
        actions = {
            // Live / offline pill
            val liveColor = if (uiState.isConnected) BullGreen else BearRed
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(liveColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(liveColor, RoundedCornerShape(3.dp)),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = if (uiState.isConnected) "LIVE" else "OFF",
                    style = MaterialTheme.typography.labelSmall,
                    color = liveColor,
                    letterSpacing = 1.sp,
                )
            }

            Spacer(Modifier.width(4.dp))

            // Dark-mode toggle
            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle theme",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Feed toggle
            val btnBg = if (uiState.isRunning) BearRed.copy(alpha = 0.15f)
                        else BullGreen.copy(alpha = 0.15f)
            val btnFg = if (uiState.isRunning) BearRed else BullGreen
            Button(
                onClick = onToggleFeed,
                modifier = Modifier.padding(end = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = btnBg,
                    contentColor = btnFg,
                ),
            ) {
                Text(
                    text = if (uiState.isRunning) "Stop" else "Start",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

// ── Tab row ───────────────────────────────────────────────────────────────────

@Composable
private fun TradingTabRow(activeTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
        ) {
            AppTab.entries.forEach { tab ->
                TradingTab(
                    label = tab.name.lowercase().replaceFirstChar { it.uppercaseChar() },
                    selected = activeTab == tab,
                    onClick = { onTabSelected(tab) },
                )
                Spacer(Modifier.width(28.dp))
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp,
        )
    }
}

@Composable
private fun TradingTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onBackground
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "tab_color",
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(200),
        label = "tab_indicator",
    )
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .alpha(indicatorAlpha)
                .background(AccentGold, RoundedCornerShape(1.dp)),
        )
    }
}

// ── Offline banner ────────────────────────────────────────────────────────────

@Composable
private fun OfflineBanner(isOffline: Boolean, cacheTimestamp: Long?) {
    AnimatedVisibility(
        visible = isOffline,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarningOrange.copy(alpha = 0.08f))
                .padding(horizontal = 20.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (cacheTimestamp != null)
                    "Cached data · ${formatStaleness(cacheTimestamp)} ago"
                else "No connection · reconnecting…",
                style = MaterialTheme.typography.labelSmall,
                color = WarningOrange,
                letterSpacing = 0.3.sp,
            )
        }
    }
}

// ── Loading / error / empty states ───────────────────────────────────────────

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = AccentGold,
                strokeWidth = 2.dp,
                modifier = Modifier.size(36.dp),
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Loading markets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorState(error: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(text = "!", fontSize = 36.sp, color = BearRed, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyMarketsState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No data available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Stock list ────────────────────────────────────────────────────────────────

@Composable
private fun PriceList(
    stocks: List<StockUiModel>,
    watchlist: Set<String>,
    selectedSymbol: String?,
    onStockClick: (String) -> Unit,
    onWatchlistToggle: (String) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(stocks, key = { it.symbol }) { stock ->
            StockRow(
                stock = stock,
                isSelected = stock.symbol == selectedSymbol,
                isWatched = stock.symbol in watchlist,
                onClick = { onStockClick(stock.symbol) },
                onWatchlistToggle = { onWatchlistToggle(stock.symbol) },
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.5.dp,
            )
        }
    }
}

// ── Stock row ─────────────────────────────────────────────────────────────────

@Composable
fun StockRow(
    stock: StockUiModel,
    isSelected: Boolean = false,
    isWatched: Boolean = false,
    onClick: () -> Unit = {},
    onWatchlistToggle: () -> Unit = {},
) {
    val isPositive = stock.change >= 0
    val changeColor = if (isPositive) BullGreen else BearRed

    val rowBg by animateColorAsState(
        targetValue = when {
            stock.flashColor != null ->
                (if (stock.change >= 0) BullGreen else BearRed).copy(alpha = 0.08f)
            isSelected ->
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "row_bg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Symbol badge
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stock.symbol.take(2),
                color = AccentGold,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }

        Spacer(Modifier.width(14.dp))

        // Symbol + absolute change
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stock.symbol,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${if (isPositive) "+" else ""}${"%.2f".format(stock.change)}",
                style = MaterialTheme.typography.bodySmall,
                color = changeColor,
                fontFamily = FontFamily.Monospace,
            )
        }

        // Price + percentage chip
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "%.2f".format(stock.price),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(3.dp))
            PriceChip(
                text = "${if (isPositive) "+" else ""}${"%.2f".format(stock.changePercentage)}%",
                color = changeColor,
            )
        }

        // Watchlist toggle
        IconButton(
            onClick = onWatchlistToggle,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = if (isWatched) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = if (isWatched) "Remove from watchlist" else "Add to watchlist",
                tint = if (isWatched) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Watchlist empty state ─────────────────────────────────────────────────────

@Composable
private fun WatchlistEmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Your watchlist is empty",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tap ★ on any row to track it",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Stock detail panel (chart + order book) ───────────────────────────────────

@Composable
private fun StockChartPanel(
    stock: StockUiModel,
    alerts: List<PriceAlert>,
    onDismiss: () -> Unit,
    onSetAlert: () -> Unit,
    onRemoveAlert: (String) -> Unit,
) {
    val isPositive = stock.change >= 0
    val accentColor = if (isPositive) BullGreen else BearRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column {
                    Text(
                        text = stock.symbol,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    // Big price
                    Text(
                        text = "%.2f".format(stock.price),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    // Change chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PriceChip(
                            text = "${if (isPositive) "+" else ""}${"%.2f".format(stock.change)}",
                            color = accentColor,
                        )
                        PriceChip(
                            text = "${if (isPositive) "+" else ""}${"%.2f".format(stock.changePercentage)}%",
                            color = accentColor,
                        )
                    }
                }

                // Actions
                Row {
                    IconButton(onClick = onSetAlert) {
                        Icon(
                            imageVector = if (alerts.isNotEmpty()) Icons.Default.NotificationsActive
                                          else Icons.Outlined.NotificationsNone,
                            contentDescription = "Set price alert",
                            tint = if (alerts.isNotEmpty()) AccentGold
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Price chart ───────────────────────────────────────────────────
            if (stock.priceHistory.size >= 2) {
                PriceLineChart(
                    prices = stock.priceHistory,
                    lineColor = accentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Waiting for price data…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ── Alerts ────────────────────────────────────────────────────────
            if (alerts.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp,
                )
                Spacer(Modifier.height(4.dp))
                alerts.forEach { alert ->
                    AlertRow(alert = alert, onRemove = { onRemoveAlert(alert.id) })
                }
            }
        }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun PriceChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun AlertRow(alert: PriceAlert, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val conditionText = when (alert.condition) {
            AlertCondition.ABOVE -> "above"
            AlertCondition.BELOW -> "below"
        }
        Text(
            text = "Alert when $conditionText ${"%.2f".format(alert.targetPrice)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove alert",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

private fun formatStaleness(cachedAt: Long): String {
    val seconds = (System.currentTimeMillis() - cachedAt) / 1_000
    return when {
        seconds < 60    -> "${seconds}s"
        seconds < 3_600 -> "${seconds / 60}m"
        else            -> "${seconds / 3_600}h"
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFF0B0E11)
@Composable
private fun TopBarPreview() {
    PricePulseTheme {
        TradingTopBar(
            uiState = PriceTrackerUiState(isConnected = true, isRunning = true),
            onToggleFeed = {},
            onToggleDarkMode = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0E11)
@Composable
private fun StockRowPreview() {
    PricePulseTheme {
        Column {
            StockRow(stock = StockUiModel("AAPL", 182.50, 0.50, 0.27), isWatched = true)
            StockRow(stock = StockUiModel("TSLA", 245.10, -3.20, -1.29))
            StockRow(stock = StockUiModel("NVDA", 875.40, 12.30, 1.43), isSelected = true)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0E11)
@Composable
private fun MarketsTabPreview() {
    val history = listOf(98f, 99f, 100f, 101f, 100.5f, 102f, 101.5f, 103f)
    PricePulseTheme {
        PriceTrackerScreenContent(
            uiState = PriceTrackerUiState(
                stocks = listOf(
                    StockUiModel("AAPL", 182.50, 0.50, 0.27, priceHistory = history),
                    StockUiModel("GOOG", 175.30, -1.20, -0.68),
                    StockUiModel("TSLA", 245.10, 4.50, 1.87),
                    StockUiModel("NVDA", 875.40, 12.30, 1.43),
                ),
                watchlist = setOf("AAPL"),
                activeTab = AppTab.MARKETS,
                isConnected = true,
                isRunning = true,
                selectedSymbol = "AAPL",
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0E11)
@Composable
private fun WatchlistEmptyPreview() {
    PricePulseTheme {
        WatchlistEmptyState()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0E11)
@Composable
private fun LoadingPreview() {
    PricePulseTheme { LoadingState() }
}
