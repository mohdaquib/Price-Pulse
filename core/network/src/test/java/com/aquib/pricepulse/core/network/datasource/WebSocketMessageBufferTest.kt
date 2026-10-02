package com.aquib.pricepulse.core.network.datasource

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebSocketMessageBufferTest {

    @Test
    fun `drops oldest pending messages when slow collector fills buffer`() = runTest {
        val buffer = WebSocketMessageBuffer(capacity = 3)
        val releaseCollector = CompletableDeferred<Unit>()
        val received = mutableListOf<String>()

        val collector = launch(start = CoroutineStart.UNDISPATCHED) {
            buffer.messages.collect { message ->
                received += message

                if (message == "0") {
                    releaseCollector.await()
                }
            }
        }

        assertTrue(buffer.offer("0"))
        runCurrent()

        // The collector is blocked, so only three pending values fit.
        assertTrue(buffer.offer("1"))
        assertTrue(buffer.offer("2"))
        assertTrue(buffer.offer("3"))
        assertTrue(buffer.offer("4"))
        assertTrue(buffer.offer("5"))

        releaseCollector.complete(Unit)
        runCurrent()

        // "1" and "2" were the oldest pending values and were discarded.
        assertEquals(
            listOf("0", "3", "4", "5"),
            received,
        )

        collector.cancelAndJoin()
    }

    @Test
    fun `does not replay stale messages to late collector`() = runTest {
        val buffer = WebSocketMessageBuffer(capacity = 3)

        assertTrue(buffer.offer("stale"))

        val received = async(start = CoroutineStart.UNDISPATCHED) {
            buffer.messages.first()
        }

        assertTrue(buffer.offer("fresh"))

        assertEquals("fresh", received.await())
    }

    @Test
    fun `broadcasts messages to all active collectors`() = runTest {
        val buffer = WebSocketMessageBuffer(capacity = 3)

        val firstCollector = async(start = CoroutineStart.UNDISPATCHED) {
            buffer.messages.take(3).toList()
        }

        val secondCollector = async(start = CoroutineStart.UNDISPATCHED) {
            buffer.messages.take(3).toList()
        }

        listOf("A", "B", "C").forEach { message ->
            assertTrue(buffer.offer(message))
        }

        assertEquals(listOf("A", "B", "C"), firstCollector.await())
        assertEquals(listOf("A", "B", "C"), secondCollector.await())
    }
}