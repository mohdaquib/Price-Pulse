package com.realtimepricetracker.data.datasource

import com.google.gson.Gson
import com.realtimepricetracker.data.config.DataConstants
import com.realtimepricetracker.data.dto.FinnhubQuoteResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinnhubRestDataSource @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson
) {
    suspend fun getQuotes(symbols: List<String>): Result<List<Pair<String, FinnhubQuoteResponseDto>>> =
        withContext(Dispatchers.IO) {
            try {
                val results = symbols.map { symbol ->
                    val request = Request.Builder()
                        .url("${DataConstants.BASE_URL}/quote?symbol=$symbol&token=${DataConstants.API_KEY}")
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
