package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.repositories.PriceRepository

/**
 * Use case for managing symbol subscriptions on the WebSocket.
 */
class WatchSymbolsUseCase(private val priceRepository: PriceRepository) {
    suspend fun subscribe(symbols: List<String>): Result<Unit> = 
        priceRepository.subscribeToSymbols(symbols)

    suspend fun unsubscribe(symbols: List<String>): Result<Unit> = 
        priceRepository.unsubscribeFromSymbols(symbols)
}
