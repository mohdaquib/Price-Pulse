package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.entities.Stock
import com.aquib.pricepulse.domain.repositories.PriceRepository
import javax.inject.Inject

class SendPriceUpdateUseCase @Inject constructor(private val priceRepository: PriceRepository) {
    suspend operator fun invoke(stock: Stock): Result<Unit> = priceRepository.sendPriceUpdate(stock)
}
