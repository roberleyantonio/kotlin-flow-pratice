# Kotlin Flow for Android: Mentorship Reference Guide

This guide serves as a quick reference for the Kotlin Flow concepts covered in our mentorship sessions. It connects reactive programming theories with real-world Android development scenarios.

---

## Module 1: The Foundation (Cold Flows)
**File:** `1_ColdFlows.kt`  
**Concept:** Cold flows are "lazy". The code inside a `flow { ... }` builder does not run until a terminal operator like `collect` is called. Every time you collect, it triggers a new execution from scratch. They are perfect for on-demand data fetching (e.g., Room database queries or single API calls).



* **Example 1: Simulating an API Call:** Demonstrates how a flow emits values over time, pausing execution with `delay` to simulate network latency without blocking the main thread.
* **Example 2: Handling Network Errors:** Shows how to use the `catch` operator to intercept exceptions (like HTTP 500 errors) and emit a fallback UI state instead of crashing the app.
* **Example 3: Transforming Data:** Uses functional operators like `map` and `filter` to format raw data (e.g., filtering negative transactions) before it reaches the UI.

---

## Module 2: State Management (StateFlow)
**File:** `2_StateFlows.kt`  
**Concept:** `StateFlow` is a "Hot Flow" that holds a single, updatable state. It always has an initial value and retains the latest value, making it the modern replacement for `LiveData`. It is the backbone of the Unidirectional Data Flow (UDF) architecture in Android ViewModels.



* **Example 1: Basic UI State Management:** Illustrates how to hold UI states (like "Loading" or "Success") so that the UI survives configuration changes (screen rotations) without losing data.
* **Example 2: Converting Cold to Hot (`stateIn`):** The golden rule for Room/API flows. Converts a cold database flow into a hot state flow, ensuring that rotating the screen doesn't trigger unnecessary re-queries to the local database.
* **Example 3: Thread-Safe State Updates:** Demonstrates the `.update { }` block, which guarantees atomic updates, preventing race conditions when multiple threads or rapid user clicks try to modify the state simultaneously.

---

## Module 3: Events & Overload Protection (SharedFlow)
**File:** `3_SharedFlows.kt`  
**Concept:** `SharedFlow` is a "Hot Flow" designed for events, not state. It does not have an initial value. If a collector is not actively listening when an event is emitted, the event is lost (unless a `replay` cache is configured). Ideal for Snackbars, Toasts, and Navigation.



* **Example 1: One-Time UI Events:** Fires a navigation event. Proves that unlike `StateFlow`, navigating away and returning won't cause the app to re-trigger the navigation (avoids the "repeating Toast" bug).
* **Example 2: Replaying Past Events:** Uses `replay = 1` to create a "sticky" broadcast. Useful for critical system alerts (like "Low Battery") that the user must see even if they opened the screen after the event was fired.
* **Example 3: Throttling Rapid Clicks:** Uses `extraBufferCapacity` and `BufferOverflow.DROP_OLDEST` to protect the app from users double-tapping or mashing buttons, safely ignoring obsolete rapid clicks.

---

## Module 4: Combine & Merge (Orchestrating Data)
**File:** `4_CombineAndMerge.kt`  
**Concept:** Android screens rarely depend on a single data source. Operators like `combine` and `zip` allow you to merge multiple flows (e.g., user input + database list) into a single, cohesive `UiState`. Knowing when to use flows versus standard coroutines for parallel tasks is a key architectural decision.


* **Example 1: Search Filter (`combine`):** Merges a `StateFlow` holding a search query with a flow of database items. Emits a new filtered list immediately whenever *either* the query or the list changes.
* **Example 2: Form Validation (`combine`):** Combines email and password input flows to dynamically evaluate and emit a boolean determining if the "Submit" button should be enabled.
* **Example 3 & 4.3: Zip vs Combine Deep Dive:** A classic Senior Developer interview topic.
    * `combine` always uses the latest available values from both flows, emitting whenever *any* flow updates (great for UI state).
    * `zip` strictly waits to form *new, unused pairs*. The faster flow must wait for the slower one (great for pairing strict coordinates or interdependent data).
* **Example 4.1: Multiple Endpoints (`combine` for 3+ Flows):** Demonstrates that `combine` natively accepts 3 (or more) arguments, waiting until all of them have emitted at least once before generating the final UI state.
* **Example 4.2: Parallel API Calls (`async`/`await`):** The golden rule for Android. If you are making 3 independent, one-shot API calls that do not emit continuous streams of data, using `Flow` is overkill. Use `async` to fire them in parallel and `await()` to gather the results cleanly.
---

## Module 5: Advanced Operators (Resilience & Lifecycle)
**File:** `5_AdvancedOperators.kt`  
**Concept:** Advanced operators that manipulate flow execution dynamically based on user behavior or network stability, separating junior from senior implementations.



* **Example 1: Cancelling Obsolete Requests (`flatMapLatest`):** The standard approach for Search bars. If the user types a new character, the previous network request is automatically cancelled, saving bandwidth and preventing race conditions where older, slower responses overwrite newer ones.
* **Example 2: Auto-Recovering from Errors (`retry`):** Automatically restarts the upstream flow execution if an exception occurs (e.g., a network timeout), drastically simplifying resilience logic without requiring complex `while/try-catch` blocks.