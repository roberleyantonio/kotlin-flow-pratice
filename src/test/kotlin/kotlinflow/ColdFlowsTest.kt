package kotlinflow

import app.cash.turbine.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ColdFlowsTest {

    // Helper functions simulating the Repository layer
    private fun fetchTransactions(): Flow<String> = flow {
        delay(500)
        emit("Transaction A: $50.00")
        delay(500)
        emit("Transaction B: $12.50")
    }

    private fun fetchProfileWithError(): Flow<String> = flow {
        emit("Loading user profile...")
        delay(300)
        throw RuntimeException("500 Internal Server Error")
    }.catch {
        emit("Failed to load profile.")
    }

    private fun getTransformedData(): Flow<String> = flowOf(150, -20, 300)
        .filter { it > 0 }
        .map { "Income: $$it" }

    // --- TESTS START HERE ---

    @Test
    fun `test successful api call with delays`() = runTest {
        // runTest skips delays automatically! A 1000ms delay runs in 0ms.
        fetchTransactions().test {
            // We assert the exact order of emissions
            assertEquals("Transaction A: $50.00", awaitItem())
            assertEquals("Transaction B: $12.50", awaitItem())

            // We must explicitly say that we expect the flow to finish
            awaitComplete()
        }
    }

    @Test
    fun `test network error is caught and fallback state is emitted`() = runTest {
        fetchProfileWithError().test {
            // First emission before the crash
            assertEquals("Loading user profile...", awaitItem())

            // The catch block intercepts the crash and emits this
            assertEquals("Failed to load profile.", awaitItem())

            awaitComplete()
        }
    }

    @Test
    fun `test data transformation ignores negative values`() = runTest {
        getTransformedData().test {
            // 150 is positive
            assertEquals("Income: $150", awaitItem())

            // -20 is ignored completely, so the next item must be 300
            assertEquals("Income: $300", awaitItem())

            awaitComplete()
        }
    }
}