package com.realtimepricetracker.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for Finnhub WebSocket trade data.
 */
data class FinnhubTradeDto(
    @SerializedName("data") val data: List<TradeData> = emptyList(),
    @SerializedName("type") val type: String = ""
)

data class TradeData(
    @SerializedName("p") val price: Double,
    @SerializedName("s") val symbol: String,
    @SerializedName("t") val timestamp: Long,
    @SerializedName("v") val volume: Double
)

/**
 * DTO for Finnhub REST quote response.
 */
data class FinnhubQuoteResponseDto(
    @SerializedName("c") val currentPrice: Double,
    @SerializedName("d") val change: Double,
    @SerializedName("dp") val percentChange: Double,
    @SerializedName("h") val highPrice: Double,
    @SerializedName("l") val lowPrice: Double,
    @SerializedName("o") val openPrice: Double,
    @SerializedName("pc") val previousClose: Double,
    @SerializedName("t") val timestamp: Long
)
