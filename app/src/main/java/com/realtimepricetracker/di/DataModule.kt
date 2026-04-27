package com.realtimepricetracker.di

import android.content.Context
import com.google.gson.Gson
import com.realtimepricetracker.data.datasource.FinnhubRestDataSource
import com.realtimepricetracker.data.datasource.WebSocketDataSource
import com.realtimepricetracker.data.local.AlertDataSource
import com.realtimepricetracker.data.local.StockCacheDataSource
import com.realtimepricetracker.data.local.WatchlistDataSource
import com.realtimepricetracker.data.notification.NotificationHelper
import com.realtimepricetracker.data.repositories.AlertRepositoryImpl
import com.realtimepricetracker.data.repositories.ConnectionRepositoryImpl
import com.realtimepricetracker.data.repositories.PriceRepositoryImpl
import com.realtimepricetracker.data.repositories.WatchlistRepositoryImpl
import com.realtimepricetracker.domain.repositories.AlertRepository
import com.realtimepricetracker.domain.repositories.ConnectionRepository
import com.realtimepricetracker.domain.repositories.PriceRepository
import com.realtimepricetracker.domain.repositories.WatchlistRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideAppScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideWebSocketDataSource(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): WebSocketDataSource = WebSocketDataSource(scope, context)

    @Provides
    @Singleton
    fun provideRestDataSource(client: OkHttpClient, gson: Gson): FinnhubRestDataSource = 
        FinnhubRestDataSource(client, gson)

    @Provides
    @Singleton
    fun provideStockCacheDataSource(
        @ApplicationContext context: Context,
        gson: Gson
    ): StockCacheDataSource = StockCacheDataSource(context, gson)

    @Provides
    @Singleton
    fun provideWatchlistDataSource(
        @ApplicationContext context: Context
    ): WatchlistDataSource = WatchlistDataSource(context)

    @Provides
    @Singleton
    fun provideAlertDataSource(
        @ApplicationContext context: Context,
        gson: Gson
    ): AlertDataSource = AlertDataSource(context, gson)

    @Provides
    @Singleton
    fun provideNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper = NotificationHelper(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPriceRepository(
        impl: PriceRepositoryImpl
    ): PriceRepository

    @Binds
    @Singleton
    abstract fun bindConnectionRepository(
        impl: ConnectionRepositoryImpl
    ): ConnectionRepository

    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(
        impl: WatchlistRepositoryImpl
    ): WatchlistRepository

    @Binds
    @Singleton
    abstract fun bindAlertRepository(
        impl: AlertRepositoryImpl
    ): AlertRepository
}
