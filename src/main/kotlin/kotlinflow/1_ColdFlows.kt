package kotlinflow


import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() = runBlocking {
    println("--- MODULE 1: COLD FLOWS ---")

    println("\nExample 1: Simulating an API Call")
    simulateApiCall()

    println("\nExample 2: Handling Network Errors")
    handleNetworkErrors()

    println("\nExample 3: Transforming Data (DTO to Domain)")
    transformData()
}

// EXAMPLE 1: Basic Cold Flow emitting data over time (Simulating Pagination or Room)
suspend fun simulateApiCall() {
    val transactionFlow = flow {
        println("[Repository] Fetching transactions from server...")
        delay(500) // Simulating network latency
        emit("Transaction A: $50.00")
        delay(500)
        emit("Transaction B: $12.50")
    }

    // The flow only starts when we call collect()
    println("[ViewModel] Collecting data:")
    transactionFlow.collect { transaction ->
        println("[UI] Displaying: $transaction")
    }
}

// EXAMPLE 2: Catching exceptions gracefully without crashing the app
suspend fun handleNetworkErrors() {
    val unstableApiFlow = flow {
        emit("Loading user profile...")
        delay(300)
        throw RuntimeException("500 Internal Server Error")
    }

    unstableApiFlow
        .catch { exception ->
            // Intercepting the crash and emitting a fallback UI state
            println("[Repository] Error caught: ${exception.message}")
            emit("Failed to load profile. Show retry button.")
        }
        .collect { uiState ->
            println("[UI] State updated to: $uiState")
        }
}

// EXAMPLE 3: Using map and filter to process data before it reaches the UI
suspend fun transformData() {
    val rawDatabaseFlow = flowOf(150, -20, 300, -5, 45) // Positive = Income, Negative = Expense

    rawDatabaseFlow
        .filter { amount -> amount > 0 } // Keep only income
        .map { income -> "Income: $$income" } // Transform into a UI string
        .collect { formattedString ->
            println("[UI] Green text: $formattedString")
        }
}