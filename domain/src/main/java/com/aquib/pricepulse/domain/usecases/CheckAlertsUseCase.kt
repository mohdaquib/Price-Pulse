package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.entities.AlertCondition
import com.aquib.pricepulse.domain.entities.PriceAlert
import javax.inject.Inject

class CheckAlertsUseCase @Inject constructor() {
    operator fun invoke(alerts: List<PriceAlert>, symbol: String, price: Double): List<PriceAlert> =
        alerts.filter { alert ->
            alert.symbol == symbol && alert.isActive && when (alert.condition) {
                AlertCondition.ABOVE -> price >= alert.targetPrice
                AlertCondition.BELOW -> price <= alert.targetPrice
            }
        }
}
