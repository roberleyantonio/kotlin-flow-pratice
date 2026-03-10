package kotlinflow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun main() = runBlocking {
    println("--- MODULE 2: STATE FLOWS ---")

    println("\nExample 1: Basic UI State Management")
    basicUiStateManagement()

    println("\nExample 2: Converting Cold to Hot (stateIn)")
    convertingColdToHotState()

    println("\nExample 3: Thread-Safe State Updates")
    threadSafeStateUpdate()
}

// EXAMPLE 1: Holding the UI state for configuration changes (screen rotations)
suspend fun basicUiStateManagement() {
    coroutineScope {
        val uiState = MutableStateFlow("Loading Screen...")

        val uiJob = launch {
            uiState.collect { state ->
                println("[UI Fragment] Rendering: $state")
            }
        }

        delay(300)
        uiState.value = "Profile Loaded: John Doe"
        delay(300)

        uiJob.cancel()
    }
}

// EXAMPLE 2: Best practice for Room/API. Converting a Cold Flow to a Hot StateFlow
suspend fun convertingColdToHotState() {
    coroutineScope {
        val coldDatabaseFlow = flow {
            println("[Room DB] Querying database...")
            delay(200)
            emit("User Settings Data")
        }

        val hotStateFlow = coldDatabaseFlow.stateIn(
            scope = this,
            started = SharingStarted.Lazily,
            initialValue = "Fetching..."
        )

        println("[UI] First observer connects:")
        val job1 = launch { hotStateFlow.collect { println("Observer 1: $it") } }
        delay(400)

        println("[UI] User rotated the screen. Second observer connects:")
        val job2 = launch { hotStateFlow.collect { println("Observer 2: $it") } }

        delay(200)
        job1.cancel()
        job2.cancel()
    }
}

// EXAMPLE 3: Using the .update { } block to avoid race conditions
suspend fun threadSafeStateUpdate() {
    coroutineScope {
        data class CartState(val items: Int = 0)
        val cartState = MutableStateFlow(CartState())

        launch {
            cartState.update { currentState -> currentState.copy(items = currentState.items + 1) }
        }
        launch {
            cartState.update { currentState -> currentState.copy(items = currentState.items + 1) }
        }

        delay(100)
        println("[UI] Total items in cart: ${cartState.value.items} (Should be 2)")
    }
}