package com.aquib.pricepulse.domain.entities

data class Stock(
    val symbol: String,
    val price: Double,
    val change: Double,
    val changePercentage: Double = 0.0
)
