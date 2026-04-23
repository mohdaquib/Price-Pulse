package com.realtimepricetracker.data.datasource

import com.realtimepricetracker.data.config.DataConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * Data source for WebSocket communication.
 * Handles low-level network operations and connection management.
 */
class WebSocketDataSource(private val scope: CoroutineScope) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    private val _receivedMessages = MutableSharedFlow<String>(replay = 1)
    val receivedMessages: SharedFlow<String> = _receivedMessages

    fun connect() {
        if (_connectionState.value) return
        
        val request = Request.Builder().url(DataConstants.WS_URL).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch { _receivedMessages.emit(text) }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = false
            }
        })
    }

    fun subscribe(symbol: String) {
        val message = String.format(DataConstants.SUBSCRIBE_MESSAGE, symbol)
        send(message)
    }

    fun unsubscribe(symbol: String) {
        val message = String.format(DataConstants.UNSUBSCRIBE_MESSAGE, symbol)
        send(message)
    }

    fun subscribeMultiple(symbols: List<String>) {
        symbols.forEach { subscribe(it) }
    }

    fun unsubscribeMultiple(symbols: List<String>) {
        symbols.forEach { unsubscribe(it) }
    }

    private fun send(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
        _connectionState.value = false
        webSocket = null
    }

    fun isConnected(): Boolean = _connectionState.value
}
