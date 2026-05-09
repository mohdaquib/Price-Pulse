package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.repositories.AlertRepository
import javax.inject.Inject

class RemoveAlertUseCase @Inject constructor(private val repository: AlertRepository) {
    suspend operator fun invoke(id: String) = repository.remove(id)
}
