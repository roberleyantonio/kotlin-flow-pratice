package kotlinflow

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StateFlowsTest {

    // 1. Simulating an Android ViewModel
    class ProfileViewModel {
        private val _uiState = MutableStateFlow("Loading Screen...")
        val uiState = _uiState // Exposing read-only state

        fun loadProfileComplete() {
            _uiState.value = "Profile Loaded: John Doe"
        }
    }

    class CartViewModel {
        data class CartState(val items: Int = 0)

        private val _cartState = MutableStateFlow(CartState())
        val cartState = _cartState

        fun addItemThreadSafe() {
            _cartState.update { currentState ->
                currentState.copy(items = currentState.items + 1)
            }
        }
    }

    // --- TESTS START HERE ---

    @Test
    fun `test StateFlow emits initial value immediately and receives updates`() = runTest {
        val viewModel = ProfileViewModel()

        viewModel.uiState.test {
            // RULE 1: StateFlow ALWAYS emits its current value the moment you collect it.
            assertEquals("Loading Screen...", awaitItem())

            // Simulating a user action or API response
            viewModel.loadProfileComplete()

            // RULE 2: We await the new state triggered by the action
            assertEquals("Profile Loaded: John Doe", awaitItem())

            // RULE 3: StateFlows NEVER complete natively.
            // We must explicitly tell Turbine to cancel the collection, otherwise the test hangs forever.
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test StateFlow ignores duplicate consecutive values (Conflation)`() = runTest {
        val state = MutableStateFlow("A")

        state.test {
            assertEquals("A", awaitItem()) // Initial value

            state.value = "B"
            assertEquals("B", awaitItem()) // State changed

            state.value = "B" // Setting the EXACT same value again
            state.value = "C"

            // Notice we don't await for the second "B". StateFlow ignores it!
            // It only emits when the value actually changes.
            assertEquals("C", awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test thread safe updates using update block`() = runTest {
        val viewModel = CartViewModel()

        viewModel.cartState.test {
            // Initial state
            assertEquals(0, awaitItem().items)

            // Triggering the update
            viewModel.addItemThreadSafe()
            assertEquals(1, awaitItem().items)

            viewModel.addItemThreadSafe()
            assertEquals(2, awaitItem().items)

            cancelAndIgnoreRemainingEvents()
        }
    }
}