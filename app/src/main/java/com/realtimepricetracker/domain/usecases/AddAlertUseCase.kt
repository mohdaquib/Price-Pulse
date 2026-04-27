package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.entities.PriceAlert
import com.realtimepricetracker.domain.repositories.AlertRepository
import javax.inject.Inject

class AddAlertUseCase @Inject constructor(private val repository: AlertRepository) {
    suspend operator fun invoke(alert: PriceAlert) = repository.add(alert)
}
