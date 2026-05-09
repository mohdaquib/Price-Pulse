package com.aquib.pricepulse.data.repositories

import com.aquib.pricepulse.core.network.datasource.FinnhubRestDataSource
import com.aquib.pricepulse.core.network.datasource.WebSocketDataSource
import com.aquib.pricepulse.core.network.dto.FinnhubTradeDto
import com.aquib.pricepulse.data.local.StockCacheDataSource
import com.aquib.pricepulse.data.mapper.toDomain
import com.aquib.pricepulse.domain.entities.Stock
import com.aquib.pricepulse.domain.repositories.PriceRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepositoryImpl @Inject constructor(
    private val webSocketDataSource: WebSocketDataSource,
    private val restDataSource: FinnhubRestDataSource,
    private val gson: Gson,
    private val stockCacheDataSource: StockCacheDataSource,
) : PriceRepository {

    private val priceCache = mutableMapOf<String, Double>()

    override suspend fun getStocks(symbols: List<String>): Result<List<Stock>> {
        return try {
            restDataSource.getQuotes(symbols).fold(
                onSuccess = { quotes ->
                    val stocks = quotes.map { (symbol, quoteDto) ->
                        Stock(
                            symbol = symbol,
                            price = quoteDto.currentPrice,
                            change = quoteDto.change,
                            changePercentage = quoteDto.percentChange,
                        ).also { priceCache[symbol] = quoteDto.currentPrice }
                    }.sortedByDescending { it.price }

                    if (stocks.isNotEmpty()) stockCacheDataSource.save(stocks)
                    Result.success(stocks)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun subscribeToPriceUpdates(): Flow<Result<Stock>> =
        webSocketDataSource.receivedMessages.map { message ->
            try {
                val tradeDto = gson.fromJson(message, FinnhubTradeDto::class.java)
                if (tradeDto.type == "trade" && tradeDto.data.isNotEmpty()) {
                    val trade = tradeDto.data.first()
                    val previousPrice = priceCache[trade.symbol] ?: trade.price
                    priceCache[trade.symbol] = trade.price
                    Result.success(trade.toDomain(previousPrice))
                } else {
                    null
                }
            } catch (e: Exception) {
                Result.failure<Stock>(e)
            }
        }.filterNotNull()

    override suspend fun subscribeToSymbols(symbols: List<String>): Result<Unit> = try {
        webSocketDataSource.subscribeMultiple(symbols)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun unsubscribeFromSymbols(symbols: List<String>): Result<Unit> = try {
        webSocketDataSource.unsubscribeMultiple(symbols)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCachedStocks(): Pair<List<Stock>, Long?> = stockCacheDataSource.load()
}
