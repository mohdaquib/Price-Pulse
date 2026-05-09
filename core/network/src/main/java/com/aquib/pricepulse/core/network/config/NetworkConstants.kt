package com.aquib.pricepulse.core.network.config

object NetworkConstants {
    const val API_KEY = "your-api-key"

    const val WS_URL = "wss://ws.finnhub.io?token=${API_KEY}"
    const val BASE_URL = "https://finnhub.io/api/v1"

    const val SUBSCRIBE_MESSAGE = """{"type":"subscribe","symbol":"%s"}"""
    const val UNSUBSCRIBE_MESSAGE = """{"type":"unsubscribe","symbol":"%s"}"""
}
