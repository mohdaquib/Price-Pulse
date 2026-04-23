package com.realtimepricetracker.di

import com.google.gson.Gson
import com.realtimepricetracker.data.datasource.FinnhubRestDataSource
import com.realtimepricetracker.data.datasource.WebSocketDataSource
import com.realtimepricetracker.data.repositories.ConnectionRepositoryImpl
import com.realtimepricetracker.data.repositories.PriceRepositoryImpl
import com.realtimepricetracker.domain.repositories.ConnectionRepository
import com.realtimepricetracker.domain.repositories.PriceRepository
import com.realtimepricetracker.domain.usecases.GetInitialStocksUseCase
import com.realtimepricetracker.domain.usecases.ManageConnectionUseCase
import com.realtimepricetracker.domain.usecases.SubscribeToPriceUpdatesUseCase
import com.realtimepricetracker.domain.usecases.WatchSymbolsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Service Locator for dependency injection.
 * A simple singleton-based DI container for managing application dependencies.
 */
object AppFactory {
    private val appScope = CoroutineScope(SupervisorJob())
    private val gson = Gson()
    private val restDataSource = FinnhubRestDataSource(gson = gson)

    // Data sources
    private val webSocketDataSource by lazy {
        WebSocketDataSource(appScope)
    }

    // Repositories
    val priceRepository: PriceRepository by lazy {
        PriceRepositoryImpl(webSocketDataSource, restDataSource = restDataSource, gson = gson)
    }

    val connectionRepository: ConnectionRepository by lazy {
        ConnectionRepositoryImpl(webSocketDataSource)
    }

    // Use cases
    val getInitialStocksUseCase by lazy {
        GetInitialStocksUseCase(priceRepository)
    }

    val subscribeToPriceUpdatesUseCase by lazy {
        SubscribeToPriceUpdatesUseCase(priceRepository)
    }

    val watchSymbolsUseCase by lazy {
        WatchSymbolsUseCase(priceRepository)
    }

    val manageConnectionUseCase by lazy {
        ManageConnectionUseCase(connectionRepository)
    }
}
