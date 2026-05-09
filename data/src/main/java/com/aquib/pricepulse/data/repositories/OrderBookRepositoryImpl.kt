package com.aquib.pricepulse.data.repositories

import com.aquib.pricepulse.data.local.MockOrderBookDataSource
import com.aquib.pricepulse.domain.entities.OrderBook
import com.aquib.pricepulse.domain.repositories.OrderBookRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderBookRepositoryImpl @Inject constructor(
    private val dataSource: MockOrderBookDataSource,
) : OrderBookRepository {

    override fun observe(symbol: String, referencePrice: Double): Flow<OrderBook> = flow {
        while (true) {
            emit(dataSource.generateBook(symbol, referencePrice))
            delay(1_500)
        }
    }
}
