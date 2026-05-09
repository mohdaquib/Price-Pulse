package com.aquib.pricepulse.data.local

import com.aquib.pricepulse.domain.entities.OrderBook
import com.aquib.pricepulse.domain.entities.OrderBookEntry
import javax.inject.Inject

class MockOrderBookDataSource @Inject constructor() {

    fun generateBook(symbol: String, refPrice: Double): OrderBook {
        val tick = if (refPrice >= 1_000) 0.10 else 0.01
        val levels = 8

        val asks = (0 until levels).map { i ->
            OrderBookEntry(price = refPrice + tick + i * tick, quantity = (50..3_000).random().toDouble())
        }
        val bids = (0 until levels).map { i ->
            OrderBookEntry(price = refPrice - i * tick, quantity = (50..3_000).random().toDouble())
        }
        return OrderBook(symbol, bids, asks)
    }
}
