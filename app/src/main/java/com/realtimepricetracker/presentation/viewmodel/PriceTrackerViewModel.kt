package com.realtimepricetracker.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realtimepricetracker.domain.config.DomainConstants
import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.usecases.GetInitialStocksUseCase
import com.realtimepricetracker.domain.usecases.ManageConnectionUseCase
import com.realtimepricetracker.domain.usecases.SubscribeToPriceUpdatesUseCase
import com.realtimepricetracker.domain.usecases.WatchSymbolsUseCase
import com.realtimepricetracker.presentation.state.PriceTrackerUiState
import com.realtimepricetracker.presentation.state.StockUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Price Tracker screen.
 * Implements MVVM pattern with StateFlow for reactive UI state management.
 */
class PriceTrackerViewModel(
    private val getInitialStocksUseCase: GetInitialStocksUseCase,
    private val subscribeToPriceUpdatesUseCase: SubscribeToPriceUpdatesUseCase,
    private val watchSymbolsUseCase: WatchSymbolsUseCase,
    private val manageConnectionUseCase: ManageConnectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriceTrackerUiState())
    val uiState: StateFlow<PriceTrackerUiState> = _uiState.asStateFlow()

    init {
        // Initialize with stock data
        loadInitialStocks()

        // Subscribe to price updates from the data source
        subscribeToPriceUpdatesUseCase()
            .onEach { result ->
                result.onSuccess { stock ->
                    updateStockData(stock)
                }.onFailure { error ->
                    _uiState.update { it.copy(error = "Update error: ${error.message}") }
                }
            }
            .launchIn(viewModelScope)

        // Observe connection state
        manageConnectionUseCase.observeConnectionState()
            .onEach { connected ->
                _uiState.update { it.copy(isConnected = connected) }
                if (connected && _uiState.value.isRunning) {
                    // Re-subscribe if connection was lost and restored
                    watchSymbolsUseCase.subscribe(DomainConstants.STOCK_SYMBOLS)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadInitialStocks() {
        _uiState.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val result = getInitialStocksUseCase()
            result.onSuccess { stocks ->
                _uiState.update { state ->
                    state.copy(
                        stocks = stocks
                            .map { it.toUiModel() }
                            .sortedByDescending { it.price },
                        loading = false
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(
                    loading = false, 
                    error = "Failed to load initial stocks: ${error.message}"
                ) }
            }
        }
    }

    fun toggleFeed() {
        if (_uiState.value.isRunning) {
            stopFeed()
        } else {
            startFeed()
        }
    }

    private fun startFeed() {
        _uiState.update { it.copy(isRunning = true, error = null) }
        viewModelScope.launch {
            manageConnectionUseCase.connect()
            // The subscription happens when connection state becomes true in the observer
            watchSymbolsUseCase.subscribe(DomainConstants.STOCK_SYMBOLS)
        }
    }

    private fun stopFeed() {
        _uiState.update { it.copy(isRunning = false) }
        viewModelScope.launch {
            watchSymbolsUseCase.unsubscribe(DomainConstants.STOCK_SYMBOLS)
            manageConnectionUseCase.disconnect()
        }
    }

    private fun updateStockData(stock: Stock) {
        _uiState.update { state ->
            val updatedStocks = state.stocks
                .map {
                    if (it.symbol == stock.symbol) {
                        it.copy(
                            price = stock.price,
                            change = stock.change,
                            changePercentage = stock.changePercentage,
                            flashColor = if (stock.change >= 0) Color.Green else Color.Red
                        )
                    } else {
                        it
                    }
                }
                .sortedByDescending { it.price }
            state.copy(stocks = updatedStocks)
        }

        // Reset flash color after delay
        viewModelScope.launch {
            delay(500) // Shorter flash for better UX
            _uiState.update { state ->
                val resetStocks = state.stocks.map {
                    if (it.symbol == stock.symbol) it.copy(flashColor = null) else it
                }
                state.copy(stocks = resetStocks)
            }
        }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        loadInitialStocks()
    }

    override fun onCleared() {
        stopFeed()
        super.onCleared()
    }

    private fun Stock.toUiModel(): StockUiModel = StockUiModel(
        symbol = symbol,
        price = price,
        change = change,
        changePercentage = changePercentage
    )
}
