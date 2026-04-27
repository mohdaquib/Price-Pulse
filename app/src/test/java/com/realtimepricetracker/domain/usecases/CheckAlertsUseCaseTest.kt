package com.realtimepricetracker.domain.usecases

import com.realtimepricetracker.domain.entities.AlertCondition
import com.realtimepricetracker.domain.entities.PriceAlert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckAlertsUseCaseTest {

    private lateinit var useCase: CheckAlertsUseCase

    @Before
    fun setUp() {
        useCase = CheckAlertsUseCase()
    }

    private fun alert(
        symbol: String = "AAPL",
        targetPrice: Double = 200.0,
        condition: AlertCondition = AlertCondition.ABOVE,
        isActive: Boolean = true,
        id: String = "1"
    ) = PriceAlert(id = id, symbol = symbol, targetPrice = targetPrice, condition = condition, isActive = isActive)

    @Test
    fun `ABOVE triggers when price is greater than target`() {
        val result = useCase(listOf(alert(condition = AlertCondition.ABOVE, targetPrice = 200.0)), "AAPL", 201.0)
        assertEquals(1, result.size)
    }

    @Test
    fun `ABOVE triggers when price equals target`() {
        val result = useCase(listOf(alert(condition = AlertCondition.ABOVE, targetPrice = 200.0)), "AAPL", 200.0)
        assertEquals(1, result.size)
    }

    @Test
    fun `ABOVE does not trigger when price is below target`() {
        val result = useCase(listOf(alert(condition = AlertCondition.ABOVE, targetPrice = 200.0)), "AAPL", 199.99)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `BELOW triggers when price is less than target`() {
        val result = useCase(listOf(alert(condition = AlertCondition.BELOW, targetPrice = 100.0)), "AAPL", 99.0)
        assertEquals(1, result.size)
    }

    @Test
    fun `BELOW triggers when price equals target`() {
        val result = useCase(listOf(alert(condition = AlertCondition.BELOW, targetPrice = 100.0)), "AAPL", 100.0)
        assertEquals(1, result.size)
    }

    @Test
    fun `BELOW does not trigger when price is above target`() {
        val result = useCase(listOf(alert(condition = AlertCondition.BELOW, targetPrice = 100.0)), "AAPL", 100.01)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `inactive alert never triggers`() {
        val result = useCase(
            listOf(alert(condition = AlertCondition.ABOVE, targetPrice = 100.0, isActive = false)),
            "AAPL", 999.0
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `alert for different symbol is not returned`() {
        val result = useCase(
            listOf(alert(symbol = "GOOG", condition = AlertCondition.ABOVE, targetPrice = 100.0)),
            "AAPL", 200.0
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `only matching alerts returned from mixed list`() {
        val alerts = listOf(
            alert(id = "1", symbol = "AAPL", condition = AlertCondition.ABOVE, targetPrice = 150.0),
            alert(id = "2", symbol = "AAPL", condition = AlertCondition.ABOVE, targetPrice = 300.0),
            alert(id = "3", symbol = "GOOG", condition = AlertCondition.ABOVE, targetPrice = 100.0),
        )
        val result = useCase(alerts, "AAPL", 200.0)
        assertEquals(1, result.size)
        assertEquals("1", result.first().id)
    }

    @Test
    fun `empty alert list returns empty result`() {
        val result = useCase(emptyList(), "AAPL", 200.0)
        assertTrue(result.isEmpty())
    }
}
