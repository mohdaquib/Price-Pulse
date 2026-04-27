package com.realtimepricetracker.data.repositories

import com.google.gson.Gson
import com.realtimepricetracker.data.datasource.FinnhubRestDataSource
import com.realtimepricetracker.data.datasource.WebSocketDataSource
import com.realtimepricetracker.data.dto.FinnhubQuoteResponseDto
import com.realtimepricetracker.data.local.StockCacheDataSource
import com.realtimepricetracker.domain.entities.Stock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PriceRepositoryImplTest {
    private lateinit var webSocketDataSource: WebSocketDataSource
    private lateinit var restDataSource: FinnhubRestDataSource
    private lateinit var stockCacheDataSource: StockCacheDataSource
    private lateinit var messagesFlow: MutableSharedFlow<String>
    private lateinit var repo: PriceRepositoryImpl
    private val gson = Gson()

    @Before
    fun setUp() {
        webSocketDataSource = mockk(relaxed = true)
        restDataSource = mockk(relaxed = true)
        stockCacheDataSource = mockk(relaxed = true)
        messagesFlow = MutableSharedFlow(extraBufferCapacity = 64)

        every { webSocketDataSource.receivedMessages } returns messagesFlow

        repo = PriceRepositoryImpl(webSocketDataSource, restDataSource, gson, stockCacheDataSource)
    }

    // ── getStocks ────────────────────────────────────────────────────────────

    @Test
    fun `getStocks success maps DTOs to Stock list and saves to cache`() = runTest {
        val dto = FinnhubQuoteResponseDto(
            currentPrice = 150.0, change = 5.0, percentChange = 3.4,
            highPrice = 152.0, lowPrice = 148.0, openPrice = 149.0,
            previousClose = 145.0, timestamp = 0L
        )
        coEvery { restDataSource.getQuotes(any()) } returns Result.success(listOf("AAPL" to dto))

        val result = repo.getStocks(listOf("AAPL"))

        assertTrue(result.isSuccess)
        val stocks = result.getOrThrow()
        assertEquals(1, stocks.size)
        assertEquals("AAPL", stocks[0].symbol)
        assertEquals(150.0, stocks[0].price, 0.001)
        assertEquals(5.0, stocks[0].change, 0.001)
        assertEquals(3.4, stocks[0].changePercentage, 0.001)
        coVerify { stockCacheDataSource.save(stocks) }
    }

    @Test
    fun `getStocks returns failure when REST call fails`() = runTest {
        coEvery { restDataSource.getQuotes(any()) } returns Result.failure(RuntimeException("network error"))

        val result = repo.getStocks(listOf("AAPL"))

        assertTrue(result.isFailure)
    }

    // ── subscribeToPriceUpdates ──────────────────────────────────────────────

    @Test
    fun `subscribeToPriceUpdates emits correct Stock for valid trade message`() = runTest {
        val json = """{"type":"trade","data":[{"p":155.5,"s":"AAPL","t":1234567890,"v":100.0}]}"""
        val results = mutableListOf<Result<Stock>>()

        val job = launch { repo.subscribeToPriceUpdates().collect { results.add(it) } }
        runCurrent()
        messagesFlow.emit(json)
        runCurrent()

        assertTrue("Results should not be empty", results.isNotEmpty())
        val stock = results[0].getOrThrow()
        assertEquals("AAPL", stock.symbol)
        assertEquals(155.5, stock.price, 0.001)
        job.cancel()
    }

    @Test
    fun `subscribeToPriceUpdates calculates change relative to previous price from cache`() = runTest {
        // Prime the internal price cache via getStocks
        val dto = FinnhubQuoteResponseDto(
            currentPrice = 150.0, change = 0.0, percentChange = 0.0,
            highPrice = 150.0, lowPrice = 150.0, openPrice = 150.0,
            previousClose = 150.0, timestamp = 0L
        )
        coEvery { restDataSource.getQuotes(any()) } returns Result.success(listOf("AAPL" to dto))
        repo.getStocks(listOf("AAPL"))

        val json = """{"type":"trade","data":[{"p":155.0,"s":"AAPL","t":1234567890,"v":100.0}]}"""
        val results = mutableListOf<Result<Stock>>()
        val job = launch { repo.subscribeToPriceUpdates().collect { results.add(it) } }
        runCurrent()
        messagesFlow.emit(json)
        runCurrent()

        assertTrue("Results should not be empty", results.isNotEmpty())
        val stock = results[0].getOrThrow()
        assertEquals(5.0, stock.change, 0.001) // 155 - 150
        job.cancel()
    }

    @Test
    fun `subscribeToPriceUpdates filters out non-trade type messages`() = runTest {
        val json = """{"type":"ping","data":[]}"""
        val results = mutableListOf<Result<Stock>>()

        val job = launch { repo.subscribeToPriceUpdates().collect { results.add(it) } }
        runCurrent()
        messagesFlow.emit(json)
        runCurrent()

        assertTrue(results.isEmpty())
        job.cancel()
    }

    @Test
    fun `subscribeToPriceUpdates emits failure for malformed JSON`() = runTest {
        val results = mutableListOf<Result<Stock>>()

        val job = launch { repo.subscribeToPriceUpdates().collect { results.add(it) } }
        runCurrent()
        messagesFlow.emit("not-valid-json")
        runCurrent()

        assertTrue("Results should not be empty", results.isNotEmpty())
        assertTrue(results[0].isFailure)
        job.cancel()
    }

    // ── getCachedStocks ──────────────────────────────────────────────────────

    @Test
    fun `getCachedStocks delegates to stockCacheDataSource and returns its result`() = runTest {
        val cachedStocks = listOf(Stock("TSLA", 800.0, 10.0, 1.25))
        coEvery { stockCacheDataSource.load() } returns Pair(cachedStocks, 1_700_000_000L)

        val (stocks, timestamp) = repo.getCachedStocks()

        assertEquals(cachedStocks, stocks)
        assertEquals(1_700_000_000L, timestamp)
    }

    // ── subscribeToSymbols / unsubscribeFromSymbols ──────────────────────────

    @Test
    fun `subscribeToSymbols calls subscribeMultiple on WebSocket`() = runTest {
        repo.subscribeToSymbols(listOf("AAPL", "GOOG"))

        verify { webSocketDataSource.subscribeMultiple(listOf("AAPL", "GOOG")) }
    }

    @Test
    fun `unsubscribeFromSymbols calls unsubscribeMultiple on WebSocket`() = runTest {
        repo.unsubscribeFromSymbols(listOf("AAPL"))

        verify { webSocketDataSource.unsubscribeMultiple(listOf("AAPL")) }
    }
}
