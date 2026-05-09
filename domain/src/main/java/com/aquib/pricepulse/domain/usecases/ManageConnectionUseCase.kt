package com.aquib.pricepulse.domain.usecases

import com.aquib.pricepulse.domain.repositories.ConnectionRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ManageConnectionUseCase @Inject constructor(
    private val connectionRepository: ConnectionRepository
) {
    val connectionState: StateFlow<Boolean> get() = connectionRepository.connectionState
    suspend fun connect(): Result<Unit> = connectionRepository.connect()
    suspend fun disconnect(): Result<Unit> = connectionRepository.disconnect()
}
