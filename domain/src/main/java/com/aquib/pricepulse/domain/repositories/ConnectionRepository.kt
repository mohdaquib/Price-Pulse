package com.aquib.pricepulse.domain.repositories

import kotlinx.coroutines.flow.StateFlow

interface ConnectionRepository {
    val connectionState: StateFlow<Boolean>
    suspend fun connect(): Result<Unit>
    suspend fun disconnect(): Result<Unit>
}
