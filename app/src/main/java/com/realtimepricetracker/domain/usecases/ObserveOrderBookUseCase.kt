package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.entities.OrderBook
import com.realtimepricetracker.domain.entities.OrderBookEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Emits a refreshed mock [OrderBook] every 1.5 seconds centred on [referencePrice].
 *
 * Mock data is used because real order-book streaming requires a Finnhub premium subscription.
 * The price levels and quantities fluctuate on each emission to simulate a live book.
 */
class ObserveOrderBookUseCase @Inject constructor() {

    operator fun invoke(symbol: String, referencePrice: Double): Flow<OrderBook> = flow {
        while (true) {
            emit(generateBook(symbol, referencePrice))
            delay(1_500)
        }
    }

    private fun generateBook(symbol: String, refPrice: Double): OrderBook {
        val tick = if (refPrice >= 1_000) 0.10 else 0.01
        val levels = 8

        val asks = (0 until levels).map { i ->
            OrderBookEntry(
                price = refPrice + tick + i * tick,
                quantity = (50..3_000).random().toDouble(),
            )
        }

        val bids = (0 until levels).map { i ->
            OrderBookEntry(
                price = refPrice - i * tick,
                quantity = (50..3_000).random().toDouble(),
            )
        }

        return OrderBook(symbol, bids, asks)
    }
}
