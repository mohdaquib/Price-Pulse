package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.entities.AlertCondition
import com.realtimepricetracker.domain.entities.PriceAlert
import javax.inject.Inject

// Pure function — no I/O. Filters the in-memory alert list for ones that have triggered.
class CheckAlertsUseCase @Inject constructor() {
    operator fun invoke(alerts: List<PriceAlert>, symbol: String, price: Double): List<PriceAlert> =
        alerts.filter { alert ->
            alert.symbol == symbol && alert.isActive && when (alert.condition) {
                AlertCondition.ABOVE -> price >= alert.targetPrice
                AlertCondition.BELOW -> price <= alert.targetPrice
            }
        }
}
