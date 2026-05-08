package com.aquib.pricepulse.core.network.datasource

import com.google.gson.Gson
import com.aquib.pricepulse.core.common.util.DispatcherProvider
import com.aquib.pricepulse.core.network.config.NetworkConstants
import com.aquib.pricepulse.core.network.dto.FinnhubQuoteResponseDto
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinnhubRestDataSource @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun getQuotes(symbols: List<String>): Result<List<Pair<String, FinnhubQuoteResponseDto>>> =
        withContext(dispatchers.io) {
            try {
                val results = symbols.map { symbol ->
                    val request = Request.Builder()
                        .url("${NetworkConstants.BASE_URL}/quote?symbol=$symbol&token=${NetworkConstants.API_KEY}")
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                        val body = response.body.string()
                        symbol to gson.fromJson(body, FinnhubQuoteResponseDto::class.java)
                    }
                }
                Result.success(results)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
