package com.aquib.pricepulse.core.network.di

import android.content.Context
import com.google.gson.Gson
import com.aquib.pricepulse.core.network.datasource.FinnhubRestDataSource
import com.aquib.pricepulse.core.network.datasource.WebSocketDataSource
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
object NetworkModule {

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
    fun provideFinnhubRestDataSource(
        client: OkHttpClient,
        gson: Gson
    ): FinnhubRestDataSource = FinnhubRestDataSource(client, gson)
}
