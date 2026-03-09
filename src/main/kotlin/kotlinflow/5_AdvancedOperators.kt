package kotlinflow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() = runBlocking {
    println("--- MODULE 5: ADVANCED OPERATORS ---")

    println("\nExample 1: Cancelling obsolete requests (flatMapLatest)")
    searchWithFlatMapLatest()

    println("\nExample 2: Auto-recovering from errors (retry)")
    autoRetryNetworkCalls()
}

// EXAMPLE 1: flatMapLatest (The Search Bar Auto-Cancel)
// This is crucial for Android! If the user types "A", then "AB",
// the network call for "A" is cancelled immediately to save data and battery.
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
suspend fun searchWithFlatMapLatest() {
    coroutineScope {
        val searchInput = MutableStateFlow("")

        val searchResultsJob = launch {
            searchInput
                .debounce(300) // 1. Wait 300ms for the user to stop typing
                .filter { query -> query.isNotEmpty() } // 2. Ignore empty queries
                .flatMapLatest { query ->
                    // 3. If a new query arrives, the previous delay/API call inside here is CANCELLED!
                    simulateNetworkSearch(query)
                }
                .collect { result ->
                    println("[UI] Showing results for: $result")
                }
        }

        delay(100)
        println("[User] Types 'Kot'")
        searchInput.value = "Kot"

        delay(100) // Typed very fast, before 300ms debounce!
        println("[User] Types 'Kotlin'")
        searchInput.value = "Kotlin" // This will cancel the "Kot" request!

        delay(1500)
        searchResultsJob.cancel()
    }
}

// Helper function simulating a slow API call
fun simulateNetworkSearch(query: String): Flow<String> = flow {
    println("[Network] Starting search for: '$query'...")
    delay(500) // Simulating server processing time
    emit("Results for $query: [Item 1, Item 2]")
}

// EXAMPLE 2: Making your app resilient to bad network conditions.
// The retry operator will automatically restart the flow if an exception is thrown.
suspend fun autoRetryNetworkCalls() {
    coroutineScope {
        var attemptCount = 0

        val flakyApiFlow = flow {
            attemptCount++
            println("[Network] Attempt $attemptCount: Connecting to server...")
            delay(200)

            if (attemptCount < 3) {
                println("[Network] Connection dropped!")
                throw RuntimeException("Network Timeout")
            } else {
                emit("Success: Downloaded 100MB of data.")
            }
        }

        val downloadJob = launch {
            flakyApiFlow
                .retry(retries = 3) { exception ->
                    // We can inspect the exception and decide if we want to retry.
                    val shouldRetry = exception is RuntimeException
                    if (shouldRetry) println("[System] Caught error. Retrying...")
                    shouldRetry // returns true to trigger retry
                }
                .catch { finalException ->
                    // If all retries fail, it falls back to this catch block.
                    println("[UI] Show Error Screen: ${finalException.message}")
                }
                .collect { data ->
                    println("[UI] $data")
                }
        }

        delay(1000)
        downloadJob.cancel()
    }
}