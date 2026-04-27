package com.realtimepricetracker.data.repositories

import com.realtimepricetracker.data.local.AlertDataSource
import com.realtimepricetracker.domain.entities.AlertCondition
import com.realtimepricetracker.domain.entities.PriceAlert
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AlertRepositoryImplTest {

    private lateinit var dataSource: AlertDataSource
    private lateinit var repository: AlertRepositoryImpl

    @Before
    fun setUp() {
        dataSource = mockk(relaxed = true)
        repository = AlertRepositoryImpl(dataSource)
    }

    @Test
    fun `observeAlerts returns alerts emitted by dataSource`() = runTest {
        val expected = listOf(
            PriceAlert(id = "1", symbol = "AAPL", targetPrice = 200.0, condition = AlertCondition.ABOVE)
        )
        every { dataSource.observeAlerts() } returns flowOf(expected)

        val result = repository.observeAlerts().toList()

        assertEquals(listOf(expected), result)
    }

    @Test
    fun `add delegates to dataSource`() = runTest {
        val alert = PriceAlert(id = "1", symbol = "AAPL", targetPrice = 200.0, condition = AlertCondition.ABOVE)

        repository.add(alert)

        coVerify { dataSource.add(alert) }
    }

    @Test
    fun `remove delegates to dataSource`() = runTest {
        repository.remove("alert-id-42")

        coVerify { dataSource.remove("alert-id-42") }
    }
}
