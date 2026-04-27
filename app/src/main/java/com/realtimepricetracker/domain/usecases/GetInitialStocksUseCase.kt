package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.config.DomainConstants
import com.realtimepricetracker.domain.entities.Stock
import com.realtimepricetracker.domain.repositories.PriceRepository
import javax.inject.Inject

class GetInitialStocksUseCase @Inject constructor(private val priceRepository: PriceRepository) {
    suspend operator fun invoke(): Result<List<Stock>> = priceRepository.getStocks(DomainConstants.STOCK_SYMBOLS)
}
