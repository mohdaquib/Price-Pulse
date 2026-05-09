package com.aquib.pricepulse.domain.repositories

import com.aquib.pricepulse.domain.entities.Stock
import kotlinx.coroutines.flow.Flow

interface PriceRepository {
    suspend fun getStocks(symbols: List<String>): Result<List<Stock>>
    fun subscribeToPriceUpdates(): Flow<Result<Stock>>
    suspend fun subscribeToSymbols(symbols: List<String>): Result<Unit>
    suspend fun unsubscribeFromSymbols(symbols: List<String>): Result<Unit>
    suspend fun getCachedStocks(): Pair<List<Stock>, Long?>
}
