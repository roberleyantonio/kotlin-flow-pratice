package kotlinflow

import app.cash.turbine.test
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedFlowsTest {

    // 1. Simulating an Android ViewModel for Navigation Events
    class NavigationViewModel {
        private val _navigationEvent = MutableSharedFlow<String>()
        val navigationEvent = _navigationEvent.asSharedFlow() // Read-only

        suspend fun clickLogin() {
            // Emitting the event when the user clicks
            _navigationEvent.emit("HomeScreen")
        }
    }

    // 2. Simulating a ViewModel with a Sticky Broadcast (Replay)
    class SystemViewModel {
        private val _stickyWarning = MutableSharedFlow<String>(replay = 1)
        val stickyWarning = _stickyWarning.asSharedFlow()

        fun emitWarning() {
            _stickyWarning.tryEmit("Warning: 5% Battery")
        }
    }

    // --- TESTS START HERE ---

    @Test
    fun `test SharedFlow one time event requires triggering after collection starts`() = runTest {
        val viewModel = NavigationViewModel()

        viewModel.navigationEvent.test {
            // RULE 1: There is no initial state to await here!
            // If we call awaitItem() right now, the test hangs forever.

            // RULE 2: We trigger the action INSIDE the test block so it is caught.
            viewModel.clickLogin()

            // Now we can safely expect the event to arrive.
            assertEquals("HomeScreen", awaitItem())

            // RULE 3: Just like StateFlow, SharedFlow never completes natively.
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test SharedFlow with replay remembers the last event`() = runTest {
        val viewModel = SystemViewModel()

        // We trigger the warning BEFORE we start collecting (before .test { })
        viewModel.emitWarning()

        viewModel.stickyWarning.test {
            // Because replay = 1, it remembered the warning for the late collector!
            assertEquals("Warning: 5% Battery", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test SharedFlow drops oldest events on buffer overflow`() = runTest {
        // Simulating the "Rapid Clicks" protection
        val clickEventFlow = MutableSharedFlow<String>(
            extraBufferCapacity = 1,
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        // CRITICAL DIFFERENCE: We fire the clicks BEFORE the test block starts collecting.
        // Because no one is listening, the items get stuck in the buffer.
        // The buffer only holds 1 item, so Clicks 1 and 2 are dropped!
        clickEventFlow.tryEmit("Click 1")
        clickEventFlow.tryEmit("Click 2")
        clickEventFlow.tryEmit("Click 3")

        clickEventFlow.test {
            // When Turbine finally connects, it only finds the survivor in the buffer
            assertEquals("Click 3", awaitItem())

            // We use expectNoEvents to prove that Clicks 1 and 2 are truly gone
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }
    }
}