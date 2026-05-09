package com.aquib.pricepulse.domain.repositories

import com.aquib.pricepulse.domain.entities.OrderBook
import kotlinx.coroutines.flow.Flow

interface OrderBookRepository {
    fun observe(symbol: String, referencePrice: Double): Flow<OrderBook>
}
