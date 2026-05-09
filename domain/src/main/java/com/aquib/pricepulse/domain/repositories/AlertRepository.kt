package com.aquib.pricepulse.domain.repositories

import com.aquib.pricepulse.domain.entities.PriceAlert
import kotlinx.coroutines.flow.Flow

interface AlertRepository {
    fun observeAlerts(): Flow<List<PriceAlert>>
    suspend fun add(alert: PriceAlert)
    suspend fun remove(id: String)
}
