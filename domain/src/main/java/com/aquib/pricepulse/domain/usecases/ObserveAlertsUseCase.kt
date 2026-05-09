package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.entities.PriceAlert
import com.aquib.pricepulse.domain.repositories.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAlertsUseCase @Inject constructor(private val repository: AlertRepository) {
    operator fun invoke(): Flow<List<PriceAlert>> = repository.observeAlerts()
}
