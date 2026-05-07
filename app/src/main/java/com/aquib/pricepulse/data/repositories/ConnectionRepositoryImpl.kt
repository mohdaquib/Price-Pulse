package com.aquib.pricepulse.data.repositories

import com.aquib.pricepulse.core.network.datasource.WebSocketDataSource
import com.aquib.pricepulse.domain.repositories.ConnectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ConnectionRepository using WebSocket data source.
 */
@Singleton
class ConnectionRepositoryImpl @Inject constructor(
    private val webSocketDataSource: WebSocketDataSource
) : ConnectionRepository {

    override fun observeConnectionState(): Flow<Boolean> {
        return webSocketDataSource.connectionState
    }

    override suspend fun connect(): Result<Unit> {
        return try {
            webSocketDataSource.connect()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun disconnect(): Result<Unit> {
        return try {
            webSocketDataSource.disconnect()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isConnected(): Boolean {
        return webSocketDataSource.isConnected()
    }
}

