package com.aquib.pricepulse.domain.entities

enum class AlertCondition { ABOVE, BELOW }

data class PriceAlert(
    val id: String,
    val symbol: String,
    val targetPrice: Double,
    val condition: AlertCondition,
    val isActive: Boolean = true
)
