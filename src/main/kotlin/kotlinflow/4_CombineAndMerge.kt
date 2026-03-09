package kotlinflow


import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    println("--- MODULE 4: COMBINE & MERGE ---")

    println("\nExample 1: Search Filter (Combine)")
    searchFilterCombination()

    println("\nExample 2: Form Validation (Combine)")
    formValidation()

    println("\nExample 3: Zip vs Combine (The 'Pairing' Operator)")
    zipVsCombine()

    println("--- MODULE 4.1: MULTIPLE ENDPOINTS ---")

    println("\nExample 4.1: Using Combine for 3 Flows")
    combineThreeFlows()

    println("\nExample 4.2: The 'Async/Await' approach (Best for one-shot API calls)")
    parallelAsyncAwait()

    println("\nEXAMPLE 4.3: The difference between Zip and Combine.")
    theDifferenceBetweenZipAndCombine()
}

// EXAMPLE 1: The classic Search Bar. Combining a search query with a list of items.
// Combine emits a new value WHENEVER ANY of the parent flows emit a new value.
suspend fun searchFilterCombination() {
    coroutineScope {
        val searchQuery = MutableStateFlow("")
        val databaseItems = MutableStateFlow(listOf("Apple", "Banana", "Cherry", "Avocado"))

        // The "Blender": Mixes both flows into a new resulting flow
        val filteredListFlow = combine(searchQuery, databaseItems) { query, items ->
            if (query.isEmpty()) {
                items
            } else {
                items.filter { it.contains(query, ignoreCase = true) }
            }
        }

        val uiJob = launch {
            filteredListFlow.collect { result ->
                println("[UI List] Showing: $result")
            }
        }

        delay(100)
        println("[User] Types 'a' in the search bar...")
        searchQuery.value = "a"

        delay(100)
        println("[User] Types 'av' in the search bar...")
        searchQuery.value = "av"

        delay(100)
        uiJob.cancel()
    }
}

// EXAMPLE 2: Enabling a Submit Button only when all fields are valid.
suspend fun formValidation() {
    coroutineScope {
        val emailFlow = MutableStateFlow("")
        val passwordFlow = MutableStateFlow("")

        val isSubmitEnabledFlow = combine(emailFlow, passwordFlow) { email, password ->
            val isEmailValid = email.contains("@")
            val isPasswordValid = password.length >= 6
            isEmailValid && isPasswordValid
        }

        val uiJob = launch {
            isSubmitEnabledFlow.collect { isEnabled ->
                println("[UI Button] Enabled state: $isEnabled")
            }
        }

        delay(100)
        println("[User] Types invalid email...")
        emailFlow.value = "john.doe" // Button stays false

        delay(100)
        println("[User] Types valid email...")
        emailFlow.value = "john.doe@gmail.com" // Button stays false (password is empty)

        delay(100)
        println("[User] Types valid password...")
        passwordFlow.value = "123456" // Button becomes TRUE!

        delay(100)
        uiJob.cancel()
    }
}

// EXAMPLE 3: Zip vs Combine.
// Combine triggers when ANY flow updates. Zip waits for a PAIR (both must update).
suspend fun zipVsCombine() {
    coroutineScope {
        val flowA = flowOf("A1", "A2", "A3").onEach { delay(100) }
        val flowB = flowOf("B1", "B2").onEach { delay(200) } // Slower flow

        println("--- Starting ZIP ---")
        // Zip will only emit "A1-B1" and "A2-B2". "A3" is ignored because B has no 3rd item.
        val zipJob = launch {
            flowA.zip(flowB) { a, b -> "$a-$b" }.collect {
                println("[ZIP Result] $it")
            }
        }
        zipJob.join() // Wait for zip to finish

        println("\n--- Starting COMBINE ---")
        // Combine will emit as soon as it has at least one from both, and then every time ANY updates.
        val flowC = flowOf("C1", "C2", "C3").onEach { delay(100) }
        val flowD = flowOf("D1", "D2").onEach { delay(250) }

        val combineJob = launch {
            combine(flowC, flowD) { c, d -> "$c-$d" }.collect {
                println("[COMBINE Result] $it")
            }
        }
        delay(500)
        combineJob.cancel()
    }
}

// EXAMPLE 4.1: Using combine with 3 endpoints.
// It waits until ALL THREE flows have emitted at least their first value.
suspend fun combineThreeFlows() {
    coroutineScope {
        // Simulating 3 different API endpoints that return Flows
        val userProfileFlow = flow { delay(200); emit("User: John") }
        val userPreferencesFlow = flow { delay(500); emit("Theme: Dark") }
        val userNotificationsFlow = flow { delay(100); emit("Alerts: 3 Unread") } // Fastest

        println("[ViewModel] Waiting for all 3 flows to emit...")

        val combinedJob = launch {
            // Combine naturally accepts 3 arguments!
            combine(userProfileFlow, userPreferencesFlow, userNotificationsFlow) { profile, prefs, alerts ->
                "Final UI State -> $profile | $prefs | $alerts"
            }.collect { finalState ->
                // This will only print AFTER 500ms (the slowest flow)
                println("[UI] $finalState")
            }
        }

        delay(800)
        combinedJob.cancel()
    }
}

// EXAMPLE 4.2: The Android Pro approach for independent one-shot API calls.
// If it's just a single request (not a continuous stream), use async instead of Flow.
suspend fun parallelAsyncAwait() {
    coroutineScope {
        println("[ViewModel] Firing 3 parallel API requests...")

        val timeTaken = measureTimeMillis {
            // async starts a background task and returns a 'Deferred' promise
            val profileDeferred = async { fetchProfileFromApi() }
            val configDeferred = async { fetchConfigFromApi() }
            val statusDeferred = async { fetchServerStatusFromApi() }

            // await() pauses this line until the specific result is ready.
            // Since they were fired in parallel, it only takes as long as the slowest one (600ms).
            val profile = profileDeferred.await()
            val config = configDeferred.await()
            val status = statusDeferred.await()

            println("[UI] Loaded all data: $profile | $config | $status")
        }

        println("[System] Total time: $timeTaken ms (Notice it didn't take 1100ms!)")
    }
}

// EXAMPLE 4: The difference between Zip and Combine.
// This is a classic Senior Developer interview question!
suspend fun theDifferenceBetweenZipAndCombine() {
    coroutineScope {
        println("\n--- ZIP vs COMBINE ---")

        // Letters Flow: Fast (emits every 100ms)
        val lettersFlow = flow {
            delay(100); emit("A")
            delay(100); emit("B")
            delay(100); emit("C")
        }

        // Numbers Flow: Slow (emits every 250ms)
        val numbersFlow = flow {
            delay(250); emit(1)
            delay(250); emit(2)
            delay(250); emit(3)
        }

        println(">>> Starting COMBINE (Notice the repetition of the latest values):")
        val combineJob = launch {
            // Combine emits whenever ANY flow emits, reusing the latest known value of the other.
            combine(lettersFlow, numbersFlow) { letter, number ->
                "$letter$number"
            }.collect { result ->
                println("[Combine] Generated: $result")
            }
        }

        delay(1000) // Wait for combine to finish so the console output doesn't mix
        combineJob.cancel()

        println("\n>>> Starting ZIP (Notice the perfect pairs, the fastest waits for the slowest):")
        val zipJob = launch {
            // Zip strictly waits for a NEW pair. Values are never reused.
            lettersFlow.zip(numbersFlow) { letter, number ->
                "$letter$number"
            }.collect { result ->
                println("[Zip] Generated: $result")
            }
        }

        delay(1000)
        zipJob.cancel()
    }
}

// Dummy suspend functions simulating Retrofit calls
suspend fun fetchProfileFromApi(): String { delay(300); return "Profile OK" }
suspend fun fetchConfigFromApi(): String { delay(600); return "Config OK" } // Slowest
suspend fun fetchServerStatusFromApi(): String { delay(200); return "Status 200" }