package com.aquib.pricepulse.data.repositories

import com.aquib.pricepulse.data.local.WatchlistDataSource
import com.aquib.pricepulse.domain.repositories.WatchlistRepository
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
