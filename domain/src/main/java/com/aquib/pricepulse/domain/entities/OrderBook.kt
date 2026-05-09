package com.aquib.pricepulse.domain.entities

/**
 * @param bids sorted descending by price (best bid first)
 * @param asks sorted ascending by price (best ask first)
 */
data class OrderBook(
    val symbol: String,
    val bids: List<OrderBookEntry>,
    val asks: List<OrderBookEntry>,
) {
    val spread: Double
        get() = if (bids.isNotEmpty() && asks.isNotEmpty())
            asks.first().price - bids.first().price
        else 0.0

    val spreadPercent: Double
        get() = if (bids.isNotEmpty() && bids.first().price > 0.0)
            (spread / bids.first().price) * 100
        else 0.0
}
