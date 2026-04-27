package com.realtimepricetracker.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realtimepricetracker.data.notification.NotificationHelper
import com.realtimepricetracker.domain.config.DomainConstants
import com.realtimepricetracker.domain.entities.AlertCondition
import com.realtimepricetracker.domain.entities.PriceAlert
import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.usecases.AddAlertUseCase
import com.realtimepricetracker.domain.usecases.AddToWatchlistUseCase
import com.realtimepricetracker.domain.usecases.CheckAlertsUseCase
import com.realtimepricetracker.domain.usecases.GetCachedStocksUseCase
import com.realtimepricetracker.domain.usecases.GetInitialStocksUseCase
import com.realtimepricetracker.domain.usecases.ManageConnectionUseCase
import com.realtimepricetracker.domain.usecases.ObserveAlertsUseCase
import com.realtimepricetracker.domain.usecases.ObserveOrderBookUseCase
import com.realtimepricetracker.domain.usecases.ObserveWatchlistUseCase
import com.realtimepricetracker.domain.usecases.RemoveAlertUseCase
import com.realtimepricetracker.domain.usecases.RemoveFromWatchlistUseCase
import com.realtimepricetracker.domain.usecases.SubscribeToPriceUpdatesUseCase
import com.realtimepricetracker.domain.usecases.WatchSymbolsUseCase
import com.realtimepricetracker.presentation.state.AppTab
import com.realtimepricetracker.presentation.state.PriceTrackerUiState
import com.realtimepricetracker.presentation.state.StockUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PriceTrackerViewModel @Inject constructor(
    private val getInitialStocksUseCase: GetInitialStocksUseCase,
    private val getCachedStocksUseCase: GetCachedStocksUseCase,
    private val subscribeToPriceUpdatesUseCase: SubscribeToPriceUpdatesUseCase,
    private val watchSymbolsUseCase: WatchSymbolsUseCase,
    private val manageConnectionUseCase: ManageConnectionUseCase,
    private val observeWatchlistUseCase: ObserveWatchlistUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
    private val observeAlertsUseCase: ObserveAlertsUseCase,
    private val addAlertUseCase: AddAlertUseCase,
    private val removeAlertUseCase: RemoveAlertUseCase,
    private val checkAlertsUseCase: CheckAlertsUseCase,
    private val notificationHelper: NotificationHelper,
    private val observeOrderBookUseCase: ObserveOrderBookUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PriceTrackerUiState())
    val uiState: StateFlow<PriceTrackerUiState> = _uiState.asStateFlow()

    private val priceHistories = mutableMapOf<String, ArrayDeque<Float>>()
    private var orderBookJob: Job? = null

    companion object {
        private const val MAX_HISTORY_SIZE = 50
    }

    init {
        loadInitialStocks()

        subscribeToPriceUpdatesUseCase()
            .onEach { result ->
                result.onSuccess { stock -> updateStockData(stock) }
                    .onFailure { error ->
                        _uiState.update { it.copy(error = "Update error: ${error.message}") }
                    }
            }
            .launchIn(viewModelScope)

        manageConnectionUseCase.observeConnectionState()
            .onEach { connected ->
                _uiState.update { it.copy(isConnected = connected) }
                if (connected && _uiState.value.isRunning) {
                    watchSymbolsUseCase.subscribe(DomainConstants.STOCK_SYMBOLS)
                }
            }
            .launchIn(viewModelScope)

        observeWatchlistUseCase()
            .onEach { watchlist -> _uiState.update { it.copy(watchlist = watchlist) } }
            .launchIn(viewModelScope)

        observeAlertsUseCase()
            .onEach { alerts -> _uiState.update { it.copy(alerts = alerts) } }
            .launchIn(viewModelScope)
    }

    private fun loadInitialStocks() {
        viewModelScope.launch {
            // Phase 1: show cache immediately so the screen is never blank while offline
            val (cached, timestamp) = getCachedStocksUseCase()
            if (cached.isNotEmpty()) {
                cached.forEach { stock ->
                    priceHistories.getOrPut(stock.symbol) { ArrayDeque() }.addLast(stock.price.toFloat())
                }
                _uiState.update { state ->
                    state.copy(
                        stocks = cached.map { it.toUiModel() }.sortedByDescending { it.price },
                        isOffline = true,
                        cacheTimestamp = timestamp,
                        loading = false,
                        error = null,
                    )
                }
            } else {
                _uiState.update { it.copy(loading = true, error = null) }
            }

            // Phase 2: fetch fresh data from REST — replaces cache on success
            val result = getInitialStocksUseCase()
            result.onSuccess { stocks ->
                if (stocks.isNotEmpty()) {
                    stocks.forEach { stock ->
                        priceHistories.getOrPut(stock.symbol) { ArrayDeque() }.addLast(stock.price.toFloat())
                    }
                    _uiState.update { state ->
                        state.copy(
                            stocks = stocks.map { it.toUiModel() }.sortedByDescending { it.price },
                            isOffline = false,
                            cacheTimestamp = null,
                            loading = false,
                            error = null,
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            loading = false,
                            error = if (state.stocks.isEmpty()) "No data available" else null,
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        loading = false,
                        error = if (state.stocks.isEmpty()) "Failed to load stocks: ${error.message}" else null,
                    )
                }
            }
        }
    }

    fun toggleFeed() {
        if (_uiState.value.isRunning) stopFeed() else startFeed()
    }

    private fun startFeed() {
        _uiState.update { it.copy(isRunning = true, error = null) }
        viewModelScope.launch {
            manageConnectionUseCase.connect()
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
        val history = priceHistories.getOrPut(stock.symbol) { ArrayDeque() }
        history.addLast(stock.price.toFloat())
        if (history.size > MAX_HISTORY_SIZE) history.removeFirst()
        val snapshot = history.toList()

        _uiState.update { state ->
            val updatedStocks = state.stocks
                .map {
                    if (it.symbol == stock.symbol) it.copy(
                        price = stock.price,
                        change = stock.change,
                        changePercentage = stock.changePercentage,
                        flashColor = if (stock.change >= 0) Color.Green else Color.Red,
                        priceHistory = snapshot
                    ) else it
                }
                .sortedByDescending { it.price }
            state.copy(stocks = updatedStocks)
        }

        // Check alerts against current in-memory list — no DataStore I/O on every tick
        val triggered = checkAlertsUseCase(_uiState.value.alerts, stock.symbol, stock.price)
        if (triggered.isNotEmpty()) {
            viewModelScope.launch {
                triggered.forEach { alert ->
                    notificationHelper.notify(alert, stock.price)
                    removeAlertUseCase(alert.id)
                }
            }
        }

        viewModelScope.launch {
            delay(500)
            _uiState.update { state ->
                state.copy(stocks = state.stocks.map {
                    if (it.symbol == stock.symbol) it.copy(flashColor = null) else it
                })
            }
        }
    }

    fun selectStock(symbol: String?) {
        orderBookJob?.cancel()
        orderBookJob = null
        _uiState.update { it.copy(selectedSymbol = symbol, orderBook = null) }
        if (symbol != null) {
            val price = _uiState.value.stocks.find { it.symbol == symbol }?.price ?: 0.0
            orderBookJob = viewModelScope.launch {
                observeOrderBookUseCase(symbol, price).collect { book ->
                    _uiState.update { it.copy(orderBook = book) }
                }
            }
        }
    }

    fun toggleWatchlist(symbol: String) {
        viewModelScope.launch {
            if (_uiState.value.watchlist.contains(symbol)) {
                removeFromWatchlistUseCase(symbol)
            } else {
                addToWatchlistUseCase(symbol)
            }
        }
    }

    fun setActiveTab(tab: AppTab) {
        _uiState.update { it.copy(activeTab = tab, selectedSymbol = null) }
    }

    fun showAlertDialog(symbol: String) {
        _uiState.update { it.copy(showAlertDialogForSymbol = symbol) }
    }

    fun dismissAlertDialog() {
        _uiState.update { it.copy(showAlertDialogForSymbol = null) }
    }

    fun addAlert(symbol: String, targetPrice: Double, condition: AlertCondition) {
        viewModelScope.launch {
            addAlertUseCase(
                PriceAlert(
                    id = UUID.randomUUID().toString(),
                    symbol = symbol,
                    targetPrice = targetPrice,
                    condition = condition
                )
            )
            dismissAlertDialog()
        }
    }

    fun removeAlert(id: String) {
        viewModelScope.launch { removeAlertUseCase(id) }
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
        changePercentage = changePercentage,
        priceHistory = priceHistories[symbol]?.toList() ?: emptyList()
    )
}
