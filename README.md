<p align="center">
  <h1 align="center">📝 KNotes</h1>
</p>

<p align="center">
  <em>A clean, simple, and modern Todo / Notes / Task / Thought-Taker App built to explore the power of Compose Multiplatform.</em>
</p>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg" alt="License"></a>
</p>

---

## 🌟 About The Project

**KNotes** is a starter project born out of the desire to explore and learn **Compose Multiplatform**. It's not a finalized product, but rather a living, breathing sandbox where we continuously build, refine, and experiment with the latest and greatest libraries in the Kotlin Multiplatform (KMP) ecosystem.

Our core idea is simple: maintain a **clean, minimalistic notes app** while constantly upgrading its internals and UI to adopt the newest features of the Compose Multiplatform stack. 

Whether you're taking quick thoughts, managing daily tasks, or just seeing how Ktor, Room, and Koin work seamlessly across platforms, this project serves as a practical, real-world example.

## 🚀 Tech Stack

We are committed to using modern, industry-standard libraries and the latest recommended approaches:

*   **UI Framework**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) - Sharing UI across Android, iOS, and Desktop.
*   **Architecture**: **MVVM + Clean Architecture principles**. We rely on modern Kotlin constructs:
    *   `StateFlow` & `SharedFlow` for reactive state management.
    *   `Coroutines` for asynchronous operations.
*   **Networking**: [Ktor 3.x](https://ktor.io/) - A lightweight, asynchronous HTTP client.
*   **Local Persistence**: [Room (Multiplatform)](https://developer.android.com/training/data-storage/room) - Robust local database using SQLite.
*   **Dependency Injection**: [Koin](https://insert-koin.io/) - Pragmatic lightweight dependency injection for Kotlin.
*   **Navigation**: `androidx.navigation` (Navigation 3) for type-safe and seamless multiplatform routing.
*   **Preferences**: DataStore for simple key-value storage.

## 🤝 Contributing

**Contributors are highly welcome!** 

We want this project to be a collaborative space to explore CMP. If you want to contribute, please keep the following guidelines in mind:

1.  **Keep the Idea Intact**: The app should remain a simple, clean notes/todo/thought app. We are adding value through *how* it's built, not necessarily by bloating it with unrelated features.
2.  **Latest Design Principles**: We strive for a beautiful, modern, and clean UI/UX. Please use the latest Material 3 guidelines or modern custom design paradigms.
3.  **Clean Code**: Adhere to clean architecture practices. Keep functions pure where possible, respect view model boundaries, and use the latest Kotlin features (Coroutines, Flows).
4.  **Explore and Upgrade**: If there's a new, stable feature in Compose Multiplatform or a core library, PRs upgrading the stack are always appreciated!

### How to Contribute
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 💻 Running the Project

### Build and Run Android Application
Use the run configuration from your IDE toolbar or run from the terminal:
```shell
./gradlew :composeApp:assembleDebug
```

### Build and Run Desktop (JVM) Application
Use the run configuration from your IDE toolbar or run from the terminal:
```shell
./gradlew :composeApp:run
```

### Build and Run iOS Application
Open the `/iosApp` directory in Xcode and run it from there, or use your IDE's Run configuration.

## 📄 License

Distributed under the Apache 2.0 License. See `LICENSE` for more information.

---
<p align="center">
  <i>Built with ❤️ and Kotlin</i>
</p>