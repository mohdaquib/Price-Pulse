package com.realtimepricetracker.data.config

object DataConstants {
    const val API_KEY = "d7ktmt1r01qiqbcvnhf0d7ktmt1r01qiqbcvnhfg" // Replace with your actual Finnhub API key

    // Finnhub WebSocket API for real-time stock data
    const val WS_URL = "wss://ws.finnhub.io?token=${API_KEY}"

    // Finnhub REST API for initial stock data
    const val BASE_URL = "https://finnhub.io/api/v1"

    // WebSocket message types
    const val SUBSCRIBE_MESSAGE = """{"type":"subscribe","symbol":"%s"}"""
    const val UNSUBSCRIBE_MESSAGE = """{"type":"unsubscribe","symbol":"%s"}"""
}
