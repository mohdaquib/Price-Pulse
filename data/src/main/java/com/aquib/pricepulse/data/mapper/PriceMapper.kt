package com.aquib.pricepulse.data.mapper

import com.aquib.pricepulse.core.network.dto.TradeData
import com.aquib.pricepulse.domain.entities.Stock

internal fun TradeData.toDomain(previousPrice: Double): Stock {
    val change = price - previousPrice
    return Stock(
        symbol = symbol,
        price = price,
        change = change,
        changePercentage = if (previousPrice != 0.0) (change / previousPrice) * 100 else 0.0,
    )
}
