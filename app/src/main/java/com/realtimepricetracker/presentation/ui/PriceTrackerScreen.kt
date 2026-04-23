package com.realtimepricetracker.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.realtimepricetracker.presentation.state.PriceTrackerUiState
import com.realtimepricetracker.presentation.state.StockUiModel
import com.realtimepricetracker.presentation.viewmodel.PriceTrackerViewModel
import com.realtimepricetracker.ui.theme.ErrorRed
import com.realtimepricetracker.ui.theme.RealtimePriceTrackerTheme
import com.realtimepricetracker.ui.theme.SuccessGreen

/**
 * Main screen for the Price Tracker application.
 */
@Composable
fun PriceTrackerScreen(viewModel: PriceTrackerViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    RealtimePriceTrackerTheme(darkTheme = uiState.isDarkMode) {
        Scaffold(
            topBar = {
                TopBar(
                    uiState = uiState,
                    onToggleFeed = { viewModel.toggleFeed() },
                    onToggleDarkMode = { viewModel.toggleDarkMode() }
                )
            },
        ) { padding ->
            PriceTrackerScreenContent(
                uiState = uiState,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

/**
 * Main content area with price list.
 */
@Composable
fun PriceTrackerScreenContent(
    uiState: PriceTrackerUiState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier
        .fillMaxSize()
        .padding(16.dp), contentAlignment = Alignment.Center) {
        when {
            uiState.loading && uiState.stocks.isEmpty() -> {
                CircularProgressIndicator()
            }

            uiState.error != null -> {
                Text(text = "Error: ${uiState.error}", color = ErrorRed)
            }

            uiState.stocks.isEmpty() -> {
                Text(text = "No data available", color = Color.Gray)
            }

            else -> {
                PriceList(stocks = uiState.stocks)
            }
        }
    }
}

@Composable
private fun PriceList(stocks: List<StockUiModel>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(stocks, key = { it.symbol }) { stock ->
            StockRow(stock = stock)
        }
    }
}

/**
 * Top app bar with connection status and controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    uiState: PriceTrackerUiState,
    onToggleFeed: () -> Unit,
    onToggleDarkMode: () -> Unit,
) {
    TopAppBar(
        title = { Text("Price Tracker") },
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Circle,
                contentDescription = "Connection status",
                tint = if (uiState.isConnected) SuccessGreen else ErrorRed,
                modifier = Modifier.padding(start = 8.dp)
            )
        },
        actions = {
            IconButton(onClick = onToggleDarkMode) {
                Icon(
                    imageVector = if (uiState.isDarkMode) {
                        Icons.Default.LightMode
                    } else {
                        Icons.Default.DarkMode
                    },
                    contentDescription = "Toggle dark mode"
                )
            }
            Button(onClick = onToggleFeed) {
                Text(if (uiState.isRunning) "Stop" else "Start")
            }
        },
    )
}

/**
 * Individual stock row in the price list.
 */
@Composable
fun StockRow(stock: StockUiModel) {
    val backgroundColor by animateColorAsState(stock.flashColor ?: Color.Transparent)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = stock.symbol, modifier = Modifier.weight(1f))
        Text(text = "%.2f".format(stock.price), modifier = Modifier.weight(1f))
        Text(
            text = "${if (stock.change > 0) "+" else ""}%.2f".format(stock.change),
            color = if (stock.change > 0) SuccessGreen else ErrorRed,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (stock.change > 0) {
                Icons.Default.ArrowUpward
            } else {
                Icons.Default.ArrowDownward
            },
            contentDescription = "Price trend",
            tint = if (stock.change > 0) SuccessGreen else ErrorRed,
        )
    }
}

// Previews
@Preview
@Composable
private fun TopBarPreview() {
    RealtimePriceTrackerTheme {
        TopBar(
            uiState = PriceTrackerUiState(
                isConnected = true,
                isRunning = true,
            ),
            onToggleFeed = {},
            onToggleDarkMode = {}
        )
    }
}

@Preview
@Composable
private fun StockRowPreview() {
    RealtimePriceTrackerTheme {
        StockRow(
            stock = StockUiModel(
                symbol = "AAPL",
                price = 100.00,
                change = 0.5,
                changePercentage = 0.5,
                flashColor = SuccessGreen,
            ),
        )
    }
}

@Preview
@Composable
private fun PriceTrackerScreenContentPreview() {
    RealtimePriceTrackerTheme {
        PriceTrackerScreenContent(
            uiState = PriceTrackerUiState(
                stocks = listOf(
                    StockUiModel(
                        symbol = "AAPL",
                        price = 100.00,
                        change = 0.5,
                        changePercentage = 0.5,
                        flashColor = SuccessGreen
                    ),
                    StockUiModel(
                        symbol = "GOOG",
                        price = 105.00,
                        change = -0.5,
                        changePercentage = -0.48,
                        flashColor = ErrorRed
                    ),
                    StockUiModel(
                        symbol = "TSLA",
                        price = 110.00,
                        change = 4.5,
                        changePercentage = 4.09,
                        flashColor = SuccessGreen
                    ),
                ),
                isConnected = true,
                isRunning = true,
            ),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

