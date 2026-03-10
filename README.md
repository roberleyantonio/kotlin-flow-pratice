# 🚀 Kotlin Flow in Practice: A Mentorship Guide

![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?logo=kotlin)
![Coroutines](https://img.shields.io/badge/Coroutines-1.7.3-success.svg)
![Turbine](https://img.shields.io/badge/Testing-Turbine-orange.svg)

## 👋 About Me
Hi, I'm **Roberley**, an Android Developer passionate about clean architecture, reactive programming, and mentoring fellow developers. You can find me on [LinkedIn](https://www.linkedin.com/in/roberleyantonio/).

## 🎯 The Goal of This Project
Kotlin Flow is one of the most powerful tools in modern Android development, but the learning curve can be steep. Documentation often lacks practical, day-to-day Android scenarios, and testing asynchronous streams can feel like a guessing game.

I created this repository to be the **ultimate, bulletproof mentorship guide to Kotlin Flow**.

Instead of building a massive, complex Android App just to show Flow concepts, this project isolates every behavior into pure, highly readable, and instantly executable Kotlin files. No emulators needed, no complex setups—just pure reactive programming.

---

## 📚 What You Will Learn (The Modules)

This repository is structured sequentially. Each module contains a `.kt` file with practical examples and a `main()` function so you can see the execution millisecond by millisecond in your console.

* **Module 1: Cold Flows (`1_ColdFlows.kt`)**
  The foundation. Learn how lazy streams work, how to simulate network latency, and how to safely catch exceptions without crashing.
* **Module 2: StateFlow (`2_StateFlows.kt`)**
  The core of Unidirectional Data Flow (UDF). See how to manage UI states, handle screen rotations, and perform thread-safe state updates using the `.update { }` block.
* **Module 3: SharedFlow (`3_SharedFlows.kt`)**
  Master one-time UI events (like Snackbars and Navigation) and learn how to protect your app from rapid button clicks using buffer overflows.
* **Module 4: Combine & Merge (`4_CombineAndMerge.kt`)**
  The "Senior" module. Learn how to orchestrate multiple data sources, build dynamic search filters, and understand the critical architectural difference between `combine` and `zip`.
* **Module 5: Advanced Operators (`5_AdvancedOperators.kt`)**
  Make your app resilient. Learn how to auto-cancel obsolete network requests with `flatMapLatest` and auto-recover from connection drops with `retry`.
* **Module 6: Unit Testing with Turbine (`/src/test/kotlin/`)**
  Stop guessing if your Flows work. This module proves every concept above using the `app.cash.turbine` library, reading streams sequentially in virtual time.

---

## ⚙️ How to Run This Project

This project was specifically designed to run flawlessly in **IntelliJ IDEA** without the need for an Android emulator.

1. Clone this repository.
2. Open the project in IntelliJ IDEA.
3. Navigate to any file inside the `src/main/kotlin/kotlinflow` package.
4. Click the **Green Play Button** next to the `fun main() = runBlocking { ... }` function.
5. Watch the reactive magic happen in the run console!

*Note: All examples strictly use `coroutineScope { }` to enforce Structured Concurrency best practices.*

---

## 📖 Reference Guides

If you are using this repository to study, make sure to check out the theoretical companion guides included in this project:
* [Flow Concepts Guide](src/main/kotlin/kotlinflow/FlowMentorshipGuide.md) - Theoretical breakdown of Modules 1 to 5.
* [Testing Flows Guide](src/test/kotlin/kotlinflow/TestingFlowsGuide.md) - The definitive rules for testing StateFlows, SharedFlows, and Cold Flows.

---
*Built with ❤️ for the Android Community.*
![License](https://img.shields.io/github/license/roberleyantonio/kotlin-flow-pratice)
