package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.repositories.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWatchlistUseCase @Inject constructor(private val repository: WatchlistRepository) {
    operator fun invoke(): Flow<Set<String>> = repository.observeWatchlist()
}
