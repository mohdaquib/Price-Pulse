package com.aquib.pricepulse.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.graphics.Color
import com.aquib.pricepulse.MainDispatcherRule
import com.aquib.pricepulse.domain.entities.AlertCondition
import com.aquib.pricepulse.domain.entities.PriceAlert
import com.aquib.pricepulse.domain.entities.Stock
import com.aquib.pricepulse.domain.repositories.Notifier
import com.aquib.pricepulse.domain.usecases.AddAlertUseCase
import com.aquib.pricepulse.domain.usecases.AddToWatchlistUseCase
import com.aquib.pricepulse.domain.usecases.CheckAlertsUseCase
import com.aquib.pricepulse.domain.usecases.GetCachedStocksUseCase
import com.aquib.pricepulse.domain.usecases.GetInitialStocksUseCase
import com.aquib.pricepulse.domain.usecases.ManageConnectionUseCase
import com.aquib.pricepulse.domain.usecases.ObserveAlertsUseCase
import com.aquib.pricepulse.domain.usecases.ObserveOrderBookUseCase
import com.aquib.pricepulse.domain.usecases.ObserveWatchlistUseCase
import com.aquib.pricepulse.domain.usecases.RemoveAlertUseCase
import com.aquib.pricepulse.domain.usecases.RemoveFromWatchlistUseCase
import com.aquib.pricepulse.domain.usecases.SubscribeToPriceUpdatesUseCase
import com.aquib.pricepulse.domain.usecases.WatchSymbolsUseCase
import com.aquib.pricepulse.feature.price.state.AppTab
import com.aquib.pricepulse.feature.price.viewmodel.PriceTrackerViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PriceTrackerViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()
    @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var getInitialStocksUseCase: GetInitialStocksUseCase
    private lateinit var getCachedStocksUseCase: GetCachedStocksUseCase
    private lateinit var subscribeToPriceUpdatesUseCase: SubscribeToPriceUpdatesUseCase
    private lateinit var watchSymbolsUseCase: WatchSymbolsUseCase
    private lateinit var manageConnectionUseCase: ManageConnectionUseCase
    private lateinit var observeWatchlistUseCase: ObserveWatchlistUseCase
    private lateinit var addToWatchlistUseCase: AddToWatchlistUseCase
    private lateinit var removeFromWatchlistUseCase: RemoveFromWatchlistUseCase
    private lateinit var observeAlertsUseCase: ObserveAlertsUseCase
    private lateinit var addAlertUseCase: AddAlertUseCase
    private lateinit var removeAlertUseCase: RemoveAlertUseCase
    private lateinit var checkAlertsUseCase: CheckAlertsUseCase
    private lateinit var notifier: Notifier
    private lateinit var observeOrderBookUseCase: ObserveOrderBookUseCase

    // Controllable flows shared across tests
    private val priceUpdatesFlow = MutableSharedFlow<Result<Stock>>(extraBufferCapacity = 16)
    private val connectionStateFlow = MutableStateFlow(false)
    private val watchlistFlow = MutableStateFlow<Set<String>>(emptySet())
    private val alertsFlow = MutableStateFlow<List<PriceAlert>>(emptyList())

    @Before
    fun setUp() {
        getInitialStocksUseCase = mockk(relaxed = true)
        getCachedStocksUseCase = mockk(relaxed = true)
        subscribeToPriceUpdatesUseCase = mockk(relaxed = true)
        watchSymbolsUseCase = mockk(relaxed = true)
        manageConnectionUseCase = mockk(relaxed = true)
        observeWatchlistUseCase = mockk(relaxed = true)
        addToWatchlistUseCase = mockk(relaxed = true)
        removeFromWatchlistUseCase = mockk(relaxed = true)
        observeAlertsUseCase = mockk(relaxed = true)
        addAlertUseCase = mockk(relaxed = true)
        removeAlertUseCase = mockk(relaxed = true)
        checkAlertsUseCase = mockk(relaxed = true)
        notifier = mockk(relaxed = true)
        observeOrderBookUseCase = mockk(relaxed = true)

        // Default stubs used by every test unless overridden
        coEvery { getCachedStocksUseCase() } returns Pair(emptyList(), null)
        coEvery { getInitialStocksUseCase(any()) } returns Result.success(emptyList())
        every { subscribeToPriceUpdatesUseCase() } returns priceUpdatesFlow
        every { manageConnectionUseCase.connectionState } returns connectionStateFlow
        every { observeWatchlistUseCase() } returns watchlistFlow
        every { observeAlertsUseCase() } returns alertsFlow
        every { checkAlertsUseCase(any(), any(), any()) } returns emptyList()
        every { observeOrderBookUseCase(any(), any()) } returns emptyFlow()
    }

    private fun createViewModel() = PriceTrackerViewModel(
        getInitialStocksUseCase = getInitialStocksUseCase,
        getCachedStocksUseCase = getCachedStocksUseCase,
        subscribeToPriceUpdatesUseCase = subscribeToPriceUpdatesUseCase,
        watchSymbolsUseCase = watchSymbolsUseCase,
        manageConnectionUseCase = manageConnectionUseCase,
        observeWatchlistUseCase = observeWatchlistUseCase,
        addToWatchlistUseCase = addToWatchlistUseCase,
        removeFromWatchlistUseCase = removeFromWatchlistUseCase,
        observeAlertsUseCase = observeAlertsUseCase,
        addAlertUseCase = addAlertUseCase,
        removeAlertUseCase = removeAlertUseCase,
        checkAlertsUseCase = checkAlertsUseCase,
        notifier = notifier,
        observeOrderBookUseCase = observeOrderBookUseCase,
    )

    // ── Loading and initial data ─────────────────────────────────────────────

    @Test
    fun `shows loading when no cache and REST is in flight`() = runTest {
        coEvery { getInitialStocksUseCase(any()) } coAnswers {
            delay(10_000)
            Result.success(emptyList())
        }

        val vm = createViewModel()
        // Cache was empty → loading=true; REST is pending (delay not yet advanced)
        assertTrue(vm.uiState.value.loading)
    }

    @Test
    fun `shows cached stocks as offline before REST returns`() = runTest {
        val cached = listOf(Stock("AAPL", 150.0, 0.0))
        coEvery { getCachedStocksUseCase() } returns Pair(cached, 1_700_000_000L)
        coEvery { getInitialStocksUseCase(any()) } coAnswers {
            delay(10_000)
            Result.success(emptyList())
        }

        val vm = createViewModel()

        assertTrue(vm.uiState.value.isOffline)
        assertEquals(1, vm.uiState.value.stocks.size)
        assertEquals(1_700_000_000L, vm.uiState.value.cacheTimestamp)
    }

    @Test
    fun `REST success clears offline flag and replaces cached stocks`() = runTest {
        val cached = listOf(Stock("AAPL", 150.0, 0.0))
        val fresh = listOf(Stock("AAPL", 160.0, 10.0), Stock("GOOG", 100.0, 2.0))
        coEvery { getCachedStocksUseCase() } returns Pair(cached, 1_700_000_000L)
        coEvery { getInitialStocksUseCase(any()) } returns Result.success(fresh)

        val vm = createViewModel()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isOffline)
        assertNull(vm.uiState.value.cacheTimestamp)
        assertEquals(2, vm.uiState.value.stocks.size)
    }

    @Test
    fun `REST failure with cached stocks does not show error`() = runTest {
        val cached = listOf(Stock("AAPL", 150.0, 0.0))
        coEvery { getCachedStocksUseCase() } returns Pair(cached, null)
        coEvery { getInitialStocksUseCase(any()) } returns Result.failure(RuntimeException("offline"))

        val vm = createViewModel()
        advanceUntilIdle()

        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `REST failure without cache shows error message`() = runTest {
        coEvery { getInitialStocksUseCase(any()) } returns Result.failure(RuntimeException("no network"))

        val vm = createViewModel()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)
    }

    // ── Feed toggle ──────────────────────────────────────────────────────────

    @Test
    fun `toggleFeed starts feed and calls connect`() = runTest {
        val vm = createViewModel()
        vm.toggleFeed()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isRunning)
        coVerify { manageConnectionUseCase.connect() }
    }

    @Test
    fun `toggleFeed twice stops feed and calls disconnect`() = runTest {
        val vm = createViewModel()
        vm.toggleFeed()
        advanceUntilIdle()
        vm.toggleFeed()
        advanceUntilIdle()

        assertFalse(vm.uiState.value.isRunning)
        coVerify { manageConnectionUseCase.disconnect() }
    }

    // ── Price updates ────────────────────────────────────────────────────────

    @Test
    fun `price update with positive change sets green flash and updates stock`() = runTest {
        coEvery { getInitialStocksUseCase(any()) } returns Result.success(
            listOf(Stock("AAPL", 150.0, 0.0))
        )
        val vm = createViewModel()
        advanceUntilIdle()

        priceUpdatesFlow.emit(Result.success(Stock("AAPL", 160.0, 10.0)))

        val stock = vm.uiState.value.stocks.find { it.symbol == "AAPL" }
        assertNotNull(stock)
        assertEquals(160.0, stock!!.price, 0.001)
        assertTrue(stock.flashColor == Color.Green)
    }

    @Test
    fun `price update with negative change sets red flash`() = runTest {
        coEvery { getInitialStocksUseCase(any()) } returns Result.success(
            listOf(Stock("AAPL", 150.0, 0.0))
        )
        val vm = createViewModel()
        advanceUntilIdle()

        priceUpdatesFlow.emit(Result.success(Stock("AAPL", 140.0, -10.0)))

        val stock = vm.uiState.value.stocks.find { it.symbol == "AAPL" }
        assertTrue(stock!!.flashColor == Color.Red)
    }

    @Test
    fun `flash color clears after 500ms`() = runTest {
        coEvery { getInitialStocksUseCase(any()) } returns Result.success(
            listOf(Stock("AAPL", 150.0, 0.0))
        )
        val vm = createViewModel()
        advanceUntilIdle()

        priceUpdatesFlow.emit(Result.success(Stock("AAPL", 160.0, 10.0)))
        advanceTimeBy(500)
        advanceUntilIdle()

        val stock = vm.uiState.value.stocks.find { it.symbol == "AAPL" }
        assertNull(stock!!.flashColor)
    }

    // ── Alert triggering ─────────────────────────────────────────────────────

    @Test
    fun `triggered alert sends notification and is removed`() = runTest {
        val alert = PriceAlert(id = "a1", symbol = "AAPL", targetPrice = 200.0, condition = AlertCondition.ABOVE)
        alertsFlow.value = listOf(alert)

        coEvery { getInitialStocksUseCase(any()) } returns Result.success(
            listOf(Stock("AAPL", 150.0, 0.0))
        )
        every { checkAlertsUseCase(any(), eq("AAPL"), eq(205.0)) } returns listOf(alert)

        createViewModel()
        advanceUntilIdle()

        priceUpdatesFlow.emit(Result.success(Stock("AAPL", 205.0, 55.0)))
        advanceUntilIdle()

        verify { notifier.notify(alert, 205.0) }
        coVerify { removeAlertUseCase("a1") }
    }

    // ── Watchlist ────────────────────────────────────────────────────────────

    @Test
    fun `toggleWatchlist adds symbol when not in watchlist`() = runTest {
        val vm = createViewModel()
        vm.toggleWatchlist("AAPL")
        advanceUntilIdle()

        coVerify { addToWatchlistUseCase("AAPL") }
    }

    @Test
    fun `toggleWatchlist removes symbol when already in watchlist`() = runTest {
        watchlistFlow.value = setOf("AAPL")
        val vm = createViewModel()
        vm.toggleWatchlist("AAPL")
        advanceUntilIdle()

        coVerify { removeFromWatchlistUseCase("AAPL") }
    }

    // ── Alert dialog and management ──────────────────────────────────────────

    @Test
    fun `addAlert calls use case with correct data and dismisses dialog`() = runTest {
        val vm = createViewModel()
        vm.showAlertDialog("AAPL")

        vm.addAlert("AAPL", 200.0, AlertCondition.ABOVE)
        advanceUntilIdle()

        coVerify { addAlertUseCase(match { it.symbol == "AAPL" && it.targetPrice == 200.0 && it.condition == AlertCondition.ABOVE }) }
        assertNull(vm.uiState.value.showAlertDialogForSymbol)
    }

    @Test
    fun `removeAlert delegates to use case`() = runTest {
        val vm = createViewModel()
        vm.removeAlert("alert-99")
        advanceUntilIdle()

        coVerify { removeAlertUseCase("alert-99") }
    }

    // ── Navigation and UI state ──────────────────────────────────────────────

    @Test
    fun `selectStock updates selectedSymbol and starts order book`() = runTest {
        coEvery { getInitialStocksUseCase(any()) } returns Result.success(
            listOf(Stock("AAPL", 150.0, 0.0))
        )
        val vm = createViewModel()
        advanceUntilIdle()

        vm.selectStock("AAPL")

        assertEquals("AAPL", vm.uiState.value.selectedSymbol)
        verify { observeOrderBookUseCase("AAPL", 150.0) }
    }

    @Test
    fun `selectStock with null clears symbol and order book`() = runTest {
        val vm = createViewModel()
        vm.selectStock("AAPL")
        vm.selectStock(null)

        assertNull(vm.uiState.value.selectedSymbol)
        assertNull(vm.uiState.value.orderBook)
    }

    @Test
    fun `setActiveTab updates activeTab`() = runTest {
        val vm = createViewModel()
        vm.setActiveTab(AppTab.WATCHLIST)

        assertEquals(AppTab.WATCHLIST, vm.uiState.value.activeTab)
    }

    @Test
    fun `clearError sets error to null`() = runTest {
        coEvery { getInitialStocksUseCase(any()) } returns Result.failure(RuntimeException("boom"))
        val vm = createViewModel()
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.error)
        vm.clearError()

        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `toggleDarkMode flips isDarkMode`() = runTest {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.isDarkMode) // default is true

        vm.toggleDarkMode()

        assertFalse(vm.uiState.value.isDarkMode)
    }
}
