package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.repositories.PriceRepository
import javax.inject.Inject

class WatchSymbolsUseCase @Inject constructor(private val priceRepository: PriceRepository) {
    suspend fun subscribe(symbols: List<String>) = priceRepository.subscribeToSymbols(symbols)
    suspend fun unsubscribe(symbols: List<String>) = priceRepository.unsubscribeFromSymbols(symbols)
}
