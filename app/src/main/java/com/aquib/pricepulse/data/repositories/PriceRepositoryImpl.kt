package com.aquib.pricepulse.data.repositories

import com.aquib.pricepulse.core.network.datasource.FinnhubRestDataSource
import com.aquib.pricepulse.core.network.datasource.WebSocketDataSource
import com.aquib.pricepulse.core.network.dto.FinnhubTradeDto
import com.aquib.pricepulse.data.local.StockCacheDataSource
import com.aquib.pricepulse.domain.entities.Stock
import com.aquib.pricepulse.domain.repositories.PriceRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepositoryImpl @Inject constructor(
    private val webSocketDataSource: WebSocketDataSource,
    private val restDataSource: FinnhubRestDataSource,
    private val gson: Gson,
    private val stockCacheDataSource: StockCacheDataSource,
) : PriceRepository {

    // Cache for previous prices to calculate changes
    private val priceCache = mutableMapOf<String, Double>()

    override suspend fun getStocks(symbols: List<String>): Result<List<Stock>> {
        return try {
            val quotesResult = restDataSource.getQuotes(symbols)

            quotesResult.fold(
                onSuccess = { quotes ->
                    val stocks = quotes.map { (symbol, quoteDto) ->
                        val stock = Stock(
                            symbol = symbol,
                            price = quoteDto.currentPrice,
                            change = quoteDto.change,
                            changePercentage = quoteDto.percentChange
                        )
                        priceCache[symbol] = quoteDto.currentPrice
                        stock
                    }.sortedByDescending { it.price }

                    if (stocks.isNotEmpty()) stockCacheDataSource.save(stocks)
                    Result.success(stocks)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun subscribeToPriceUpdates(): Flow<Result<Stock>> {
        return webSocketDataSource.receivedMessages.map { message ->
            try {
                val tradeDto = gson.fromJson(message, FinnhubTradeDto::class.java)
                if (tradeDto.type == "trade" && tradeDto.data.isNotEmpty()) {
                    val trade = tradeDto.data.first()
                    val previousPrice = priceCache[trade.symbol] ?: trade.price
                    val change = trade.price - previousPrice

                    // Update cache
                    priceCache[trade.symbol] = trade.price

                    val stock = Stock(
                        symbol = trade.symbol,
                        price = trade.price,
                        change = change,
                        changePercentage = if (previousPrice != 0.0) (change / previousPrice) * 100 else 0.0
                    )
                    Result.success(stock)
                } else {
                    null // Ignore non-trade messages
                }
            } catch (e: Exception) {
                Result.failure<Stock>(Exception("Failed to parse WebSocket message: ${e.message}"))
            }
        }.filterNotNull()
    }

    override suspend fun subscribeToSymbols(symbols: List<String>): Result<Unit> {
        return try {
            webSocketDataSource.subscribeMultiple(symbols)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unsubscribeFromSymbols(symbols: List<String>): Result<Unit> {
        return try {
            webSocketDataSource.unsubscribeMultiple(symbols)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPriceUpdate(stock: Stock): Result<Unit> = Result.success(Unit)

    override suspend fun getCachedStocks(): Pair<List<Stock>, Long?> = stockCacheDataSource.load()
}

