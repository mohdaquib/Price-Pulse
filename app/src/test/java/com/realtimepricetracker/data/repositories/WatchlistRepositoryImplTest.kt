package com.realtimepricetracker.data.repositories

import com.realtimepricetracker.data.local.WatchlistDataSource
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class WatchlistRepositoryImplTest {

    private lateinit var dataSource: WatchlistDataSource
    private lateinit var repository: WatchlistRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk(relaxed = true)
        repository = WatchlistRepositoryImpl(dataSource)
    }

    @Test
    fun `observeWatchlist returns symbols emitted by dataSource`() = runTest {
        val expected = setOf("AAPL", "GOOG")
        every { dataSource.observeSymbols() } returns flowOf(expected)

        val result = repository.observeWatchlist().toList()

        assertEquals(listOf(expected), result)
    }

    @Test
    fun `add delegates to dataSource`() = runTest {
        repository.add("AAPL")

        coVerify { dataSource.add("AAPL") }
    }

    @Test
    fun `remove delegates to dataSource`() = runTest {
        repository.remove("TSLA")

        coVerify { dataSource.remove("TSLA") }
    }
}
