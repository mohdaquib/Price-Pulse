package com.realtimepricetracker.data.repositories

import com.realtimepricetracker.data.local.WatchlistDataSource
import com.realtimepricetracker.domain.repositories.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val dataSource: WatchlistDataSource
) : WatchlistRepository {
    override fun observeWatchlist(): Flow<Set<String>> = dataSource.observeSymbols()
    override suspend fun add(symbol: String) = dataSource.add(symbol)
    override suspend fun remove(symbol: String) = dataSource.remove(symbol)
}
