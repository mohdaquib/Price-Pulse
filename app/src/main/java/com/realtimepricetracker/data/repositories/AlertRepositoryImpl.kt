package com.realtimepricetracker.data.repositories

import com.realtimepricetracker.data.local.AlertDataSource
import com.realtimepricetracker.domain.entities.PriceAlert
import com.realtimepricetracker.domain.repositories.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val dataSource: AlertDataSource
) : AlertRepository {
    override fun observeAlerts(): Flow<List<PriceAlert>> = dataSource.observeAlerts()
    override suspend fun add(alert: PriceAlert) = dataSource.add(alert)
    override suspend fun remove(id: String) = dataSource.remove(id)
}
