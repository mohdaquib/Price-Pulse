package com.aquib.pricepulse.core.network.dto

import com.google.gson.annotations.SerializedName
import com.aquib.pricepulse.domain.entities.Stock

data class PriceUpdateDto(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("price") val price: Double,
    @SerializedName("change") val change: Double
) {
    fun toDomain(): Stock = Stock(
        symbol = symbol,
        price = price,
        change = change,
        changePercentage = if (price != 0.0) (change / price) * 100 else 0.0
    )
}

fun Stock.toDto(): PriceUpdateDto = PriceUpdateDto(symbol = symbol, price = price, change = change)
