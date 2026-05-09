package com.aquib.pricepulse.domain.repositories

import com.aquib.pricepulse.domain.entities.PriceAlert

interface Notifier {
    fun notify(alert: PriceAlert, currentPrice: Double)
}
