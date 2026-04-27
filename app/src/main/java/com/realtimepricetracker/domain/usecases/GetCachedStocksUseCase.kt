package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.repositories.PriceRepository
import javax.inject.Inject

class GetCachedStocksUseCase @Inject constructor(private val priceRepository: PriceRepository) {
    suspend operator fun invoke(): Pair<List<Stock>, Long?> = priceRepository.getCachedStocks()
}
