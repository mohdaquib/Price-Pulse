package com.aquib.pricepulse.core.network.datasource

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class WebSocketMessageBuffer(
    capacity: Int = 64,
) {
    private val _messages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = capacity,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun offer(message: String): Boolean = _messages.tryEmit(message)
}