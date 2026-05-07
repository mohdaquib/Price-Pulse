package com.aquib.pricepulse.core.network.datasource

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.aquib.pricepulse.core.network.config.NetworkConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import java.util.Collections
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class WebSocketDataSource(
    private val scope: CoroutineScope,
    context: Context,
) {
    private val appContext = context.applicationContext

    // pingInterval tells OkHttp to send a WebSocket ping frame every 30 s.
    // If the server doesn't respond with a pong, OkHttp closes the socket
    // and fires onFailure — which triggers our reconnect path.
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)   // 0 = no read timeout (streaming connection)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    @Volatile private var webSocket: WebSocket? = null
    private val isConnecting = AtomicBoolean(false)
    private val shouldReconnect = AtomicBoolean(false)
    private val reconnectAttempts = AtomicInteger(0)
    @Volatile private var reconnectJob: Job? = null

    // Symbols currently subscribed — resent on reconnect since new sockets have no session memory.
    private val subscribedSymbols: MutableSet<String> =
        Collections.synchronizedSet(mutableSetOf())

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    // replay = 0: live events, not state — late collectors must not receive stale trades.
    // extraBufferCapacity = 64: lets the OkHttp thread emit without suspending.
    private val _receivedMessages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val receivedMessages: SharedFlow<String> = _receivedMessages

    init {
        registerNetworkCallback()
    }

    fun connect() {
        shouldReconnect.set(true)
        if (_connectionState.value || isConnecting.get()) return
        connectInternal()
    }

    fun subscribe(symbol: String) {
        subscribedSymbols.add(symbol)
        send(String.format(NetworkConstants.SUBSCRIBE_MESSAGE, symbol))
    }

    fun unsubscribe(symbol: String) {
        subscribedSymbols.remove(symbol)
        send(String.format(NetworkConstants.UNSUBSCRIBE_MESSAGE, symbol))
    }

    fun subscribeMultiple(symbols: List<String>) = symbols.forEach { subscribe(it) }
    fun unsubscribeMultiple(symbols: List<String>) = symbols.forEach { unsubscribe(it) }

    fun disconnect() {
        shouldReconnect.set(false)
        reconnectJob?.cancel()
        reconnectJob = null
        reconnectAttempts.set(0)
        subscribedSymbols.clear()
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = false
        isConnecting.set(false)
    }

    fun isConnected(): Boolean = _connectionState.value

    private fun connectInternal() {
        if (!isConnecting.compareAndSet(false, true)) return

        val request = Request.Builder().url(NetworkConstants.WS_URL).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = true
                isConnecting.set(false)
                reconnectAttempts.set(0)

                val snapshot = synchronized(subscribedSymbols) { subscribedSymbols.toList() }
                snapshot.forEach { symbol ->
                    webSocket.send(String.format(NetworkConstants.SUBSCRIBE_MESSAGE, symbol))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch { _receivedMessages.emit(text) }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                handleDisconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                handleDisconnect()
            }
        })
    }

    private fun handleDisconnect() {
        _connectionState.value = false
        isConnecting.set(false)
        if (shouldReconnect.get()) scheduleReconnect()
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val delayMs = reconnectDelay()
            delay(delayMs)
            if (shouldReconnect.get() && !_connectionState.value) {
                reconnectAttempts.incrementAndGet()
                connectInternal()
            }
        }
    }

    // Exponential backoff capped at 64 s, with up to 1 s jitter to spread reconnect storms.
    private fun reconnectDelay(): Long {
        val attempt = reconnectAttempts.get().coerceAtMost(6)
        val exponential = 1_000L shl attempt
        val jitter = (0..1_000).random().toLong()
        return exponential + jitter
    }

    private fun send(message: String) {
        webSocket?.send(message)
    }

    private fun registerNetworkCallback() {
        try {
            val cm = appContext.getSystemService(ConnectivityManager::class.java)
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            cm.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    if (shouldReconnect.get() && !_connectionState.value && !isConnecting.get()) {
                        reconnectJob?.cancel()
                        reconnectAttempts.set(0)
                        connectInternal()
                    }
                }

                override fun onLost(network: Network) {
                    reconnectJob?.cancel()
                }
            })
        } catch (_: Exception) {
            // Network monitoring is best-effort; reconnect still works via onFailure.
        }
    }
}
