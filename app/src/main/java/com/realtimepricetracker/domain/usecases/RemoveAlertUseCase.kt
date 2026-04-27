package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.repositories.AlertRepository
import javax.inject.Inject

class RemoveAlertUseCase @Inject constructor(private val repository: AlertRepository) {
    suspend operator fun invoke(id: String) = repository.remove(id)
}
