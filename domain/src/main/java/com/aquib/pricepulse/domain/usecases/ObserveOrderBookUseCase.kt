package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.entities.OrderBook
import com.aquib.pricepulse.domain.repositories.OrderBookRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOrderBookUseCase @Inject constructor(
    private val repository: OrderBookRepository
) {
    operator fun invoke(symbol: String, referencePrice: Double): Flow<OrderBook> =
        repository.observe(symbol, referencePrice)
}
