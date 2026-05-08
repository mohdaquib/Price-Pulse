package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.entities.Stock
import com.aquib.pricepulse.domain.repositories.PriceRepository
import javax.inject.Inject

class GetInitialStocksUseCase @Inject constructor(private val priceRepository: PriceRepository) {
    suspend operator fun invoke(symbols: List<String>): Result<List<Stock>> = priceRepository.getStocks(symbols)
}
