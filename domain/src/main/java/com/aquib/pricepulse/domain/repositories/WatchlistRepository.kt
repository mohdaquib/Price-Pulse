package com.aquib.pricepulse.domain.repositories

import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun observeWatchlist(): Flow<Set<String>>
    suspend fun add(symbol: String)
    suspend fun remove(symbol: String)
}
