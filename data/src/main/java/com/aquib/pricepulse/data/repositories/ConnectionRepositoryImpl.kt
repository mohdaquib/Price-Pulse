package com.aquib.pricepulse.data.repositories

import com.aquib.pricepulse.core.network.datasource.WebSocketDataSource
import com.aquib.pricepulse.domain.repositories.ConnectionRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepositoryImpl @Inject constructor(
    private val webSocketDataSource: WebSocketDataSource,
) : ConnectionRepository {
    override val connectionState: StateFlow<Boolean> = webSocketDataSource.connectionState

    override suspend fun connect(): Result<Unit> = try {
        webSocketDataSource.connect()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun disconnect(): Result<Unit> = try {
        webSocketDataSource.disconnect()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
