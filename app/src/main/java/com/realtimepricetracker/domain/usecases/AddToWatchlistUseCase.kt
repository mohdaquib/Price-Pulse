package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.repositories.WatchlistRepository
import javax.inject.Inject

class AddToWatchlistUseCase @Inject constructor(private val repository: WatchlistRepository) {
    suspend operator fun invoke(symbol: String) = repository.add(symbol)
}
