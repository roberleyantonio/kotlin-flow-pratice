# Kotlin Flow Unit Testing with Turbine: Mentorship Guide

## Module 1: Testing Cold Flows
**File:** `1_ColdFlowsTest.kt`  
**Core Concept:** Cold flows have a clear beginning and an end. You must consume all emitted items and explicitly verify that the flow has completed successfully.

* **Virtual Time (`runTest`):** Coroutine tests run in virtual time. A `delay(500)` inside your Flow takes 0ms to execute in the test, keeping your test suite lightning fast.
* **Consuming Items (`awaitItem()`):** Turbine's `test { ... }` block automatically collects the flow in a background coroutine. You use `awaitItem()` to assert the exact sequence of emissions.
* **The Finish Line (`awaitComplete()`):** Because Cold Flows eventually stop emitting, your test must declare `awaitComplete()` at the end. If the flow emits an unexpected item, throws an unhandled error, or doesn't close, the test will fail.

---

## Module 2: Testing UI State (StateFlow)
**File:** `2_StateFlowsTest.kt`  
**Core Concept:** `StateFlow` is infinite and always holds a value. It never completes on its own, which changes how we conclude our tests.

[Image of Kotlin Flow Turbine testing StateFlow diagram]

* **Immediate Initial Value:** The moment Turbine starts collecting a `StateFlow`, it instantly emits its current value. Your first assertion inside the `test { ... }` block will usually be `awaitItem()` to check this initial state (e.g., "Loading Screen...").
* **Testing Conflation (Ignoring Duplicates):** `StateFlow` ignores consecutive identical values. If you assign "State A" twice in a row, Turbine will only receive it once. Our tests prove that the UI is protected from redundant recompositions.
* **The Infinite Loop (`cancelAndIgnoreRemainingEvents()`):** Since `StateFlow` never completes natively, calling `awaitComplete()` will cause your test to hang indefinitely until it times out. You must end the test block with `cancelAndIgnoreRemainingEvents()` to gracefully stop the collection and pass the test.

---

## Module 3: Testing Events (SharedFlow)
**File:** `3_SharedFlowsTest.kt`  
**Core Concept:** `SharedFlow` does not have an initial value (unless a replay cache is configured). Timing is everything because if no one is listening, the event is lost forever.

[Image of Kotlin Flow SharedFlow testing timeline with Turbine]

* **Triggering Events Inside the Test:** Because standard SharedFlows (`replay = 0`) drop events if there are no active collectors, you must trigger the action (like a ViewModel method simulating a button click) inside the `test { ... }` block. This ensures Turbine is already actively collecting when the event fires.
* **Testing Replay Caches:** If a `SharedFlow` is configured with `replay = 1` (like a sticky system warning), you can fire the event before the `test { ... }` block starts. The test will prove that late subscribers still receive the cached event from memory.
* **Testing Buffer Overflows (The Turbine Speed Trap):** When testing protections against rapid clicks (like `BufferOverflow.DROP_OLDEST`), there is a critical catch. If you emit values *inside* the `test { }` block, Turbine's internal collector is so fast that it consumes them instantly—meaning the buffer never actually fills up, and `DROP_OLDEST` never triggers!
To properly test overflow behavior, configure the `SharedFlow` with `replay = 1` (to act as your buffer) and emit the items **before** the `test { }` block starts. Because no one is listening yet, the items get stuck, the buffer overflows, and the oldest items are destroyed. When Turbine finally connects, you can use `awaitItem()` to catch the sole survivor and `expectNoEvents()` to mathematically prove the older items were successfully dropped.

---


## Module 4 & 5: Testing Advanced Operators (Combine, FlatMapLatest, Retry)
**File:** `4_CombineAndMerge.kt` and `5_AdvancedOperators.kt`  
**Core Concept:** Advanced operators orchestrate multiple flows or handle complex timing and failure scenarios. Turbine makes testing these asynchronous behaviors predictable and linear.

* **Testing `combine`:** When testing combined flows, you manually update the upstream `MutableStateFlow` inputs and use `awaitItem()` to verify that the downstream result evaluates correctly at each step (e.g., verifying a form validation state changes from `false` to `true` only when all fields are valid).
* **Testing Cancellation (`flatMapLatest`):** Turbine shines when proving cancellation. By emitting multiple events rapidly into the upstream flow before the virtual `delay()` finishes, we can assert that only the result of the *latest* emission arrives at `awaitItem()`. The previous ongoing flows are killed.
* **Testing Resilience (`retry`):** You can construct a flow that intentionally throws an exception on the first collection attempt but succeeds on the second. Using Turbine, you simply assert that the final successful item is received without the test crashing, proving the `retry` block successfully intercepted the error and restarted the flow.