package kotlinflow

import app.cash.turbine.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AdvancedFlowsTest {

    // --- MODULE 4: COMBINE TESTS ---

    @Test
    fun `test combine emits new state when any parent flow changes`() = runTest {
        val emailFlow = MutableStateFlow("")
        val passwordFlow = MutableStateFlow("")

        val isFormValidFlow = combine(emailFlow, passwordFlow) { email, password ->
            email.contains("@") && password.length >= 6
        }

        isFormValidFlow.test {
            // 1. Initial state (both empty)
            assertEquals(false, awaitItem())

            // 2. User types a valid email, but password is still empty
            emailFlow.value = "admin@test.com"
            assertEquals(false, awaitItem())

            // 3. User types a valid password. Combine fires again and evaluates to true!
            passwordFlow.value = "123456"
            assertEquals(true, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- MODULE 5: FLATMAPLATEST & RETRY TESTS ---

    @Test
    fun `test flatMapLatest cancels previous ongoing operations`() = runTest {
        // Using SharedFlow to simulate user typing events without an initial state
        val searchQuery = MutableSharedFlow<String>()

        val searchResults = searchQuery
            .flatMapLatest { query ->
                flow {
                    delay(500) // Simulating network latency
                    emit("Result for $query")
                }
            }

        searchResults.test {
            // User types "Kot" and immediately types "Kotlin" before the 500ms delay finishes
            searchQuery.emit("Kot")
            searchQuery.emit("Kotlin")

            // Because of flatMapLatest, the "Kot" network call is cancelled.
            // We ONLY receive the result for "Kotlin".
            assertEquals("Result for Kotlin", awaitItem())

            // Proving that "Result for Kot" was destroyed
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test retry recovers from exceptions automatically`() = runTest {
        var attempt = 0

        val flakyNetworkFlow = flow {
            attempt++
            if (attempt == 1) {
                // Fails on the first try
                throw RuntimeException("Network Timeout")
            } else {
                // Succeeds on the second try
                emit("Success on attempt $attempt")
            }
        }

        flakyNetworkFlow
            .retry(retries = 1) { exception ->
                exception is RuntimeException
            }
            .test {
                // The flow threw an error internally, caught it, retried, and emitted success!
                assertEquals("Success on attempt 2", awaitItem())

                awaitComplete()
            }
    }
}