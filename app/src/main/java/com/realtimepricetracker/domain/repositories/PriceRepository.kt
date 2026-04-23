package com.realtimepricetracker.domain.repositories

import com.realtimepricetracker.domain.entities.Stock
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for stock price operations.
 * Implementation details are hidden from domain layer.
 */
interface PriceRepository {
    /**
     * Get initial stock data for given symbols
     */
    suspend fun getStocks(symbols: List<String>): Result<List<Stock>>

    /**
     * Subscribe to real-time price updates
     */
    fun subscribeToPriceUpdates(): Flow<Result<Stock>>

    /**
     * Start watching specific symbols for real-time updates
     */
    suspend fun subscribeToSymbols(symbols: List<String>): Result<Unit>

    /**
     * Stop watching specific symbols
     */
    suspend fun unsubscribeFromSymbols(symbols: List<String>): Result<Unit>

    /**
     * Send a price update through the network (for mock/testing)
     */
    suspend fun sendPriceUpdate(stock: Stock): Result<Unit>
}
