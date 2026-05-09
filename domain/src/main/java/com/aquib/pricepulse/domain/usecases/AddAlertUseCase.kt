package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.entities.PriceAlert
import com.aquib.pricepulse.domain.repositories.AlertRepository
import javax.inject.Inject

class AddAlertUseCase @Inject constructor(private val repository: AlertRepository) {
    suspend operator fun invoke(alert: PriceAlert) = repository.add(alert)
}
