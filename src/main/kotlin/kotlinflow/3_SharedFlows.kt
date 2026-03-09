package kotlinflow

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

fun main() = runBlocking {
    println("--- MODULE 3: SHARED FLOWS ---")

    println("\nExample 1: One-Time UI Events (Snackbars)")
    oneTimeUiEvents()

    println("\nExample 2: Replaying Past Events")
    replayPreviousEvents()

    println("\nExample 3: Throttling Rapid Clicks (Buffer Drop Oldest)")
    handlingRapidEventsWithBuffer()
}

// EXAMPLE 1: Emitting a Toast/Snackbar.
suspend fun oneTimeUiEvents() {
    coroutineScope {
        val navigationEvent = MutableSharedFlow<String>()

        val uiJob = launch {
            navigationEvent.collect { screen ->
                println("[UI Navigation] Navigating to: $screen")
            }
        }

        delay(200)
        println("[ViewModel] User clicked login button.")
        navigationEvent.emit("HomeScreen")

        delay(200)
        uiJob.cancel()
    }
}

// EXAMPLE 2: Using replay to act like a sticky broadcast.
suspend fun replayPreviousEvents() {
    coroutineScope {
        val stickyNotification = MutableSharedFlow<String>(replay = 1)

        println("[System] Emitting low battery warning...")
        stickyNotification.emit("Warning: 5% Battery Remaining")

        delay(200)
        println("[UI] User opened the notification center later.")

        val uiJob = launch {
            stickyNotification.collect { warning ->
                println("[UI Center] Displaying: $warning")
            }
        }

        delay(200)
        uiJob.cancel()
    }
}

// EXAMPLE 3: Protecting the app from rapid clicks
suspend fun handlingRapidEventsWithBuffer() {
    coroutineScope {
        val clickEventFlow = MutableSharedFlow<String>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        println("[User] Mashing the 'Pay Now' button 5 times...")
        for (i in 1..5) {
            clickEventFlow.tryEmit("Payment Click #$i")
        }

        val uiJob = launch {
            clickEventFlow.collect { click ->
                println("[ViewModel] Processing: $click")
            }
        }

        delay(200)
        uiJob.cancel()
    }
}