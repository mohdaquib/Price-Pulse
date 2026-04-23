package com.realtimepricetracker.data.datasource

import com.realtimepricetracker.data.config.DataConstants
import com.realtimepricetracker.data.dto.FinnhubQuoteResponseDto
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data source for Finnhub REST API communication.
 * Handles fetching initial stock data and quotes.
 */
class FinnhubRestDataSource(
    private val client: OkHttpClient = OkHttpClient(),
    private val gson: Gson = Gson(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Fetch quote data for a specific symbol
     */
    suspend fun getQuote(symbol: String): Result<FinnhubQuoteResponseDto> {
        return withContext(dispatcher) {
            try {
                val url = "${DataConstants.BASE_URL}/quote?symbol=$symbol&token=${DataConstants.API_KEY}"
                val request = Request.Builder().url(url).build()

                val response = client.newCall(request).execute()
                val responseBody = response.body.string()

                if (!response.isSuccessful || responseBody.isEmpty()) {
                    return@withContext Result.failure(Exception("Failed to fetch quote for $symbol: ${response.code}"))
                }

                val quote = gson.fromJson(responseBody, FinnhubQuoteResponseDto::class.java)
                Result.success(quote)
            } catch (e: JsonSyntaxException) {
                Result.failure(Exception("Failed to parse quote response for $symbol: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Network error fetching quote for $symbol: ${e.message}"))
            }
        }
    }

    /**
     * Fetch quotes for multiple symbols
     */
    suspend fun getQuotes(symbols: List<String>): Result<List<Pair<String, FinnhubQuoteResponseDto>>> {
        return try {
            val results = symbols.mapNotNull { symbol ->
                getQuote(symbol).getOrNull()?.let { symbol to it }
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch quotes: ${e.message}"))
        }
    }
}
