package com.aquib.pricepulse.data.repositories

import com.aquib.pricepulse.data.local.AlertDataSource
import com.aquib.pricepulse.domain.entities.PriceAlert
import com.aquib.pricepulse.domain.repositories.AlertRepository
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
